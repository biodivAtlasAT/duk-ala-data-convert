package duk.at.models

import duk.at.services.ListsItem
import duk.at.services.SpeciesService
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream
import java.io.IOException;
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Iterator


class Biom (val ifile: String, val ofile: String, val speciesLists: List<String>){

    fun convert(): List<BiocollectBiom> {
        val dcList = mutableListOf<BiocollectBiom>()
        try {
            val file = FileInputStream(File(ifile))
            val workbook: Workbook = XSSFWorkbook(file)
            val sheet: Sheet = workbook.getSheetAt(0)
            /* ID;TIMESTAMP;FILEORDNER;GARTEN_HASH;GARTEN_ID;ART;ANZAHL_1;ANZAHL_2;ANZAHL_ANMERKUNG;
            DATUM;SCHONMAL;ADRESSE_1;ADRESSE_2;LÄNGENGRAD;BREITENGRAD;ABGESIEDELT;ABGESIEDELT_WHY;
            ANGESIEDELT;ANGESIEDELT_WHY;EMAIL;VORNAME;NACHNAME;DATEIEN;GARTEN-FORM;*/

            for (row in sheet) {
                if (row.rowNum == 0)
                    continue
                if (row.rowNum == 10)
                    break
                var species: String? = null
                var count: Int? = null
                var recordedBy: String = ""
                var longitude: String? = null
                var latitude: String? = null
                var comment = ""
                var surveyDate: LocalDate? = null
                var imageList: MutableList<Image> = mutableListOf()

                for (cell in row) {
                    val value = when (cell.cellType) {
                        CellType.STRING -> cell.stringCellValue
                        CellType.NUMERIC -> cell.numericCellValue
                        CellType.BOOLEAN -> cell.booleanCellValue
                        else -> ""
                    }
                    if (cell.columnIndex == 9 && cell.cellType == CellType.STRING) {
                        val customFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        val dateString = cell.stringCellValue
                        surveyDate = LocalDate.parse(dateString, customFormat)
                    }
                    if (cell.columnIndex == 5)
                        species = SpeciesService.getInstance(speciesLists).getSpecies(cell.stringCellValue)
                    if (cell.columnIndex == 6) {
                        if (cell.cellType == CellType.NUMERIC)
                            count = cell.numericCellValue.toInt()
                        if (cell.cellType == CellType.STRING) {
                            if (cell.stringCellValue.startsWith(">"))
                                count = cell.stringCellValue.split(">")[1].toInt()
                            else
                                count = cell.stringCellValue.split(" ")[0].toInt()
                        }
                    }
                    if (cell.columnIndex == 8) {
                        if (cell.cellType == CellType.NUMERIC)
                            comment = cell.numericCellValue.toString()
                        if (cell.cellType == CellType.STRING)
                            comment = cell.stringCellValue
                    }
                    if (cell.columnIndex == 13) {
                        if (cell.cellType == CellType.NUMERIC)
                            longitude = cell.numericCellValue.toString()
                        if (cell.cellType == CellType.STRING)
                            longitude = cell.stringCellValue
                    }
                    if (cell.columnIndex == 14) {
                        if (cell.cellType == CellType.NUMERIC)
                            latitude = cell.numericCellValue.toString()
                        if (cell.cellType == CellType.STRING)
                            latitude = cell.stringCellValue
                    }
                    if (cell.columnIndex == 20)
                        recordedBy = cell.stringCellValue
                    if (cell.columnIndex == 21)
                        recordedBy = recordedBy + " " + cell.stringCellValue
                    if (cell.columnIndex == 22)
                        imageList = ImageList(cell.stringCellValue, recordedBy, "noLic",surveyDate).iL
                }
                val dc = BiocollectBiom( "serial",
                    surveyDate.toString(),
                    recordedBy,
                    latitude?:"0.0",
                    longitude?:"0.0",
                    species?: "no species",
                    species?: "no species",
                    count!!.toInt(),
                    comment,
                    imageList
                )
                dcList.add(dc)
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

        var rowIndex = 2
        dcList.forEach{ rowData ->
            val row = sheet.createRow(rowIndex)
            val scienitificNameCell = row.createCell(9)
            scienitificNameCell.setCellValue(rowData.species1Name)

            if (rowData.imageList.size > 0) {
                val img1UrlCell = row.createCell(15)
                img1UrlCell.setCellValue(rowData.imageList[0].url)
            }

/*                rowData.forEachIndexed { cellIndex, cellData ->
                val cell = row.createCell(cellIndex)
                when (cellData) {
                    is String -> cell.setCellValue(cellData)
                    is Double -> cell.setCellValue(cellData)
                    is Int -> cell.setCellValue(cellData.toDouble())
                    is Boolean -> cell.setCellValue(cellData)
                    is Date -> {
                        cell.setCellValue(cellData)
                        val style = workbook.createCellStyle()
                        style.dataFormat = workbook.creationHelper.createDataFormat().getFormat("dd-mm-yyyy")
                        cell.cellStyle = style
                    }
                    else -> cell.setCellValue(cellData.toString())
                }*/
            rowIndex++
        }

        FileOutputStream(ofile).use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
    }

}