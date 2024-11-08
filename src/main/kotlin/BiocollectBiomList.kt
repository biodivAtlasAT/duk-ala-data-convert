package duk.at

import duk.at.models.BiocollectBiom
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object BiocollectBiomList {
    fun createWorkbook(dcList: List<BiocollectBiom>, cli: Cli) {

  /*      val sourceFile = File(cli.template)
        val destFile = File(cli.ofile)
        sourceFile.copyTo(destFile, overwrite = true)*/

        val hl1 = listOf("serial","reinhardtsField","surveyDate","surveyStartTime","notes","recordedBy","location","locationLatitude","locationLongitude","species1.name","species1.scientificName","species1.commonName","species1.guid","individualCount1","identificationConfidence1","comments1","sightingPhoto1.url","sightingPhoto1.licence","sightingPhoto1.name","sightingPhoto1.filename","sightingPhoto1.attribution","sightingPhoto1.notes","sightingPhoto1.projectId","sightingPhoto1.projectName","sightingPhoto1.dateTaken","project_name","collectionID","occurrenceID","catalogNumber","fieldNumber","identificationRemarks","occurrenceStatus","basisOfRecord","phylum","class")
        val hl2 = listOf("Serial Number","reinhardtsField","Survey date","Survey start time","Notes","Recorded by","Site identifier (siteId)","Latitude","Longitude","Name","Scientific name","Common name","ALA identifier","How many individuals did you see?","Are you confident of the species identification?","Comments","Image URL","Licence","Image name","Image filename","Attribution","Notes","Project Id","Project name","Date taken","Project name","Collection ID","Occurrence ID","Catalog number","Field number","Identification remarks","Occurrence status","Basis of record","Phylum","Class")

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("csv")

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


 //       val file = File(cli.ofile)
//        val workbook = WorkbookFactory.create(file)

  //      val sheet = workbook.getSheet("RW_BIOM")

        val createHelper: CreationHelper = workbook.creationHelper
        val datumStyle: CellStyle = workbook.createCellStyle()
        datumStyle.dataFormat = createHelper.createDataFormat().getFormat("dd.mm.yyyy")

        /*val style = workbook.createCellStyle()*/
        val format = workbook.createDataFormat()
        val lonLatStyle: CellStyle = workbook.createCellStyle()
        lonLatStyle.dataFormat = format.getFormat("0.00000000")

        val cell = sheet.getRow(0)?.getCell(0)
        println("Inhalt der ersten Zelle: ${cell?.toString()}")

        var rowIndex = 2
        dcList.forEach{ rowData ->
            val row = sheet.createRow(rowIndex)

            row.createCell(0).setCellValue(rowData.serial)

            val c = row.createCell(2)
            c.setCellValue(rowData.surveyDate)
            c.setCellStyle(datumStyle);

            row.createCell(4).setCellValue(rowData.comments1)
            row.createCell(5).setCellValue(rowData.recordedBy)
            row.createCell(7).also {
                it.setCellValue(rowData.locationLatitude)
                it.setCellStyle(lonLatStyle);
            }
            row.createCell(8).also {
                it.setCellValue(rowData.locationLongitude)
                it.setCellStyle(lonLatStyle);
            }

            row.createCell(9).setCellValue(rowData.species1Name)
            row.createCell(10).setCellValue(rowData.species1ScientificName)
            row.createCell(11).setCellValue(rowData.species1Name)
            row.createCell(13).setCellValue(rowData.individualCount1.toDouble())

            if (rowData.imageList.size > 0) {
                row.createCell(16).setCellValue(rowData.imageList[0].url)
                row.createCell(17).setCellValue(rowData.imageList[0].license)
                row.createCell(18).setCellValue(rowData.imageList[0].name)
                row.createCell(19).setCellValue(rowData.imageList[0].fileName)
                row.createCell(20).setCellValue(rowData.imageList[0].attribution)
                row.createCell(21).setCellValue(rowData.imageList[0].notes)
                row.createCell(22).setCellValue(rowData.imageList[0].projectId)
                row.createCell(23).setCellValue(rowData.imageList[0].projectName)

                row.createCell(24).also {
                    it.setCellValue(rowData.imageList[0].dateTaken)
                    it.setCellStyle(datumStyle);
                }
            }
            rowIndex++
        }

        FileOutputStream(cli.ofile).use { outputStream ->
            workbook.write(outputStream)
        }

        workbook.close()
    }

}