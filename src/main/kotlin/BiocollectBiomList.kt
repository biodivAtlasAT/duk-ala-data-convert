package duk.at

import duk.at.models.BiocollectBiom
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.util.*

object BiocollectBiomList {
    fun createWorkbook(dcList: List<BiocollectBiom>, cli: Cli) {
        val templateInfos = readFirstTwoRows(cli.template)
        val hl1 = templateInfos.first
        val hl2 = templateInfos.second
        val sheetName = templateInfos.third

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(sheetName)

        val row = sheet.createRow(0)
        hl1.forEachIndexed { cellIndex, cellData ->
            val cell = row.createCell(cellIndex)
            cell.setCellValue(cellData.toString())
        }
        val row2 = sheet.createRow(1)
        hl2.forEachIndexed { cellIndex, cellData ->
            val cell = row2.createCell(cellIndex)
            cell.setCellValue(cellData.toString())
        }
        val uuid = UUID.randomUUID()

        val createHelper: CreationHelper = workbook.creationHelper
        val datumStyle: CellStyle = workbook.createCellStyle()
        datumStyle.dataFormat = createHelper.createDataFormat().getFormat("dd.mm.yyyy")

        val format = workbook.createDataFormat()
        val lonLatStyle: CellStyle = workbook.createCellStyle()
        lonLatStyle.dataFormat = format.getFormat("0.00000000")

        var rowIndex = 2
        dcList.forEach{ rowData ->
            val row = sheet.createRow(rowIndex)

            /*if (rowData.serial.isNotEmpty())
                row.createCell(0).setCellValue(rowData.serial)*/
            row.createCell(0).setCellValue("$uuid-${rowIndex-2}")
            val c = row.createCell(1)
            c.setCellValue(rowData.surveyDate)
            c.setCellStyle(datumStyle);

            row.createCell(3).setCellValue(rowData.comments1)
            row.createCell(4).setCellValue(rowData.recordedBy)
            row.createCell(6).also {
                it.setCellValue(rowData.locationLatitude)
                it.setCellStyle(lonLatStyle);
            }
            row.createCell(7).also {
                it.setCellValue(rowData.locationLongitude)
                it.setCellStyle(lonLatStyle);
            }

            row.createCell(8).setCellValue(rowData.species1Name)
            row.createCell(9).setCellValue(rowData.species1ScientificName)
            row.createCell(10).setCellValue(rowData.species1Name)
            row.createCell(12).setCellValue(rowData.individualCount1.toDouble())
            row.createCell(13).setCellValue(rowData.identificationConfidence1)

            if (rowData.imageList.size > 0) {
                row.createCell(15).setCellValue(rowData.imageList[0].url)
                row.createCell(16).setCellValue(rowData.imageList[0].license)
                row.createCell(17).setCellValue(rowData.imageList[0].name)
                row.createCell(18).setCellValue(rowData.imageList[0].fileName)
                row.createCell(19).setCellValue(rowData.imageList[0].attribution)
                row.createCell(20).setCellValue(rowData.imageList[0].notes)
                row.createCell(21).setCellValue(rowData.imageList[0].projectId)
                row.createCell(22).setCellValue(rowData.imageList[0].projectName)

                row.createCell(23).also {
                    it.setCellValue(rowData.imageList[0].dateTaken)
                    it.setCellStyle(datumStyle);
                }
            }
            if (rowData.projectName.isNotEmpty())
                row.createCell(24).setCellValue(rowData.projectName)
            if (rowData.identificationRemarks.isNotEmpty())
                row.createCell(29).setCellValue(rowData.identificationRemarks)
            rowIndex++
        }

        FileOutputStream(cli.ofile).use { outputStream ->
            workbook.write(outputStream)
        }

        workbook.close()
    }

    private fun readFirstTwoRows(filePath: String): Triple<MutableList<String>, MutableList<String>, String> {
        val hl1 = mutableListOf<String>()
        val hl2 = mutableListOf<String>()

        val inputStream = File(filePath).inputStream()
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        val sheetName = sheet.sheetName

        for (rowIndex in 0..1) { // Erste zwei Zeilen (Index 0 und 1)
            val row = sheet.getRow(rowIndex)
            if (row != null) {
                for (cell in row) {
                    val cellValue = when (cell.cellType) {
                        CellType.STRING -> cell.stringCellValue
                        CellType.NUMERIC -> cell.numericCellValue.toString()
                        CellType.BOOLEAN -> cell.booleanCellValue.toString()
                        else -> ""
                    }
                    if (rowIndex == 0) hl1.add(cellValue.toString())
                    if (rowIndex == 1) hl2.add(cellValue.toString())
                }
            }
        }
        return Triple(hl1, hl2, sheetName)
    }
}