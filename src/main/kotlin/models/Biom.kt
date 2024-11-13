package duk.at.models

import duk.at.*
import org.apache.logging.log4j.LogManager
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class Biom(private val cli: Cli){
    /* ID;TIMESTAMP;FILEORDNER;GARTEN_HASH;GARTEN_ID;ART;ANZAHL_1;ANZAHL_2;ANZAHL_ANMERKUNG;
    DATUM;SCHONMAL;ADRESSE_1;ADRESSE_2;LÄNGENGRAD;BREITENGRAD;ABGESIEDELT;ABGESIEDELT_WHY;
    ANGESIEDELT;ANGESIEDELT_WHY;EMAIL;VORNAME;NACHNAME;DATEIEN;GARTEN-FORM;*/

    companion object {
        private val logger: org.apache.logging.log4j.Logger? = LogManager.getLogger()
    }

    fun convert(): List<BiocollectBiom> {

        val dcList = mutableListOf<BiocollectBiom>()
        try {
            val file = FileInputStream(File(cli.ifile))
            val workbook: Workbook = XSSFWorkbook(file)
            val sheet: Sheet = workbook.getSheetAt(0)

            for (row in sheet) {
                if (row.rowNum == 0) continue
                if (row.rowNum > cli.count) break


                var species: String? = null
                var count: Int? = null
                var recordedBy: String = ""
                var longitude: Double? = null
                var latitude: String? = null
                var comment = ""
                var sightingDate: LocalDate? = null
                var surveyDate: LocalDate? = null
                var imageList: MutableList<Image> = mutableListOf()
                var serial: String = ""
                var scientificName: String? = null
                var errorList: MutableList<String> = mutableListOf()

                for (cell in row) {
                    imageList.clear()
                    if (cell.columnIndex == 0) serial = cell.makeStringFromStringOrNumeric
                    if (cell.columnIndex == 1) {
                        surveyDate = errorList.AddWhenNull(cell.makeDateStringFromString("yyyy-MM-dd"), "TIMESTAMP is incorrect!")
                    }
                    if (cell.columnIndex == 5) {
                        species = cell.stringCellValue
                        if (species.isEmpty())
                            errorList.add("column ART is empty!")
                        else
                            scientificName = errorList.AddWhenNull(cell.getScientificName(cli),"scientificName for <$species> not found in BIE or not in LIST!")
                    }
                    if (cell.columnIndex == 6) count = errorList.AddWhenNull(cell.makeIntFromStringOrNumeric, "ANZAHL_1 is incorrect!")
                    if (cell.columnIndex == 8) comment = cell.makeStringFromStringOrNumeric
                    if (cell.columnIndex == 9) sightingDate = errorList.AddWhenNull(cell.makeDateStringFromString("yyyy-MM-dd"), "TIMESTAMP is incorrect!")

                    if (cell.columnIndex == 13) longitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2,"Longitude is incorrect!")



                    if (cell.columnIndex == 14) latitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric, "Latitude is incorrect!")
                    if (cell.columnIndex == 20) recordedBy = cell.stringCellValue
                    if (cell.columnIndex == 21) recordedBy = errorList.AddWhenEmpty(recordedBy + " " + cell.stringCellValue, "VORNAME, NACHNAME is empty!")
                    //if (cell.columnIndex == 22) imageList = ImageList(cell.stringCellValue, recordedBy, "CC BY 3.0", sightingDate, errorList).iL
                }
                if (imageList.size == 0)
                    errorList.add("No valid Image found!")
                if (errorList.size == 0) {
                    dcList.add(BiocollectBiom(
                        serial,
                        surveyDate!!,
                        recordedBy,
                        latitude!!.toDouble(),
                        longitude!!,
                        species!!,
                        scientificName!!,
                        count!!.toInt(),
                        comment,
                        imageList
                    ))
                } else {
                    val err = "Error in RowNum: ${row.rowNum} <Serial: $serial>: " + errorList.joinToString(", ")
                    logger?.error(err)
                }
            }

            file.close()
            workbook.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dcList
    }

    fun createWorkbook(dcList: List<BiocollectBiom>) {
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

        val createHelper: CreationHelper = workbook.creationHelper
        val datumStyle: CellStyle = workbook.createCellStyle()
        datumStyle.dataFormat = createHelper.createDataFormat().getFormat("dd.mm.yyyy")

        /*val style = workbook.createCellStyle()*/
        val format = workbook.createDataFormat()
        val lonLatStyle: CellStyle = workbook.createCellStyle()
        lonLatStyle.dataFormat = format.getFormat("0.00000000")

        var rowIndex = 2
        dcList.forEach{ rowData ->
            val row = sheet.createRow(rowIndex)

            row.createCell(0).setCellValue(rowData.serial)

            /*val dateTimeString = rowData.surveyDate
            val formatter = DateTimeFormatter.ISO_DATE
            val dateTime = LocalDate.parse(dateTimeString, formatter)*/
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


