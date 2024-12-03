package duk.at.models

import duk.at.*
import duk.at.services.SpeciesService
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class Artenzaehlen(private val cli: Cli){
    /* ID;TIMESTAMP;FILEORDNER;GARTEN_HASH;GARTEN_ID;ART;ANZAHL_1;ANZAHL_2;ANZAHL_ANMERKUNG;
    DATUM;SCHONMAL;ADRESSE_1;ADRESSE_2;LÄNGENGRAD;BREITENGRAD;ABGESIEDELT;ABGESIEDELT_WHY;
    ANGESIEDELT;ANGESIEDELT_WHY;EMAIL;VORNAME;NACHNAME;DATEIEN;GARTEN-FORM;*/

    companion object {
        private val logger: org.apache.logging.log4j.Logger? = LogManager.getLogger()
    }
    val dcList = mutableListOf<BiocollectBiom>()
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val formatter2 = DateTimeFormatter.ofPattern("d/MM/yyyy")

    fun convertCSV(){
        File(cli.ifile).useLines { lines ->
            lines.drop(1).take(cli.count).forEach { line ->
                var species: String?
                var count: Int?
                var recordedBy: String
                var longitude: Double?
                var latitude: Double?
                var comment: String
                var sightingDate: LocalDateTime?
                var surveyDate: LocalDate?
                var imageList: MutableList<Image>
                var serial: String
                var scientificName: String? = null
                var errorList: MutableList<String> = mutableListOf()

                val cols = line.split(";")

                serial = cols[0]
                surveyDate = convDate(cols[1])
                species = cols[5]
                if (species.isEmpty())
                    errorList.add("column ART is empty!")
                else
                    scientificName = SpeciesService.getInstance(
                        cli.speciesLists.split(",").toList(),
                        cli.listsUrl
                    ).getSpecies(species, cli.bieUrl, "")
                count = convCount(cols[6])
                comment = cols[8]
                sightingDate = convDate2(cols[9])
                longitude = cols[13].toDoubleOrNull()
                latitude = cols[14].toDoubleOrNull()
                recordedBy = cols[20].trim() + " " + cols[21].trim()
                imageList = ImageList(cols[22], recordedBy, "CC BY 3.0", sightingDate, errorList).iL

                if (surveyDate == null) errorList.add("Error in SurveyDate!")
                if (scientificName == null) errorList.add("scientificName <$scientificName> not found in BIE or not in specified LISTS!")
                if (count == null) errorList.add("Count is not set correctly!")
                if (longitude == null) errorList.add("Longitude is not set correctly!")
                if (latitude == null) errorList.add("Latitude is not set correctly!")
                if (longitude == 0.0) errorList.add("Longitude is not set correctly!")
                if (latitude == 0.0) errorList.add("Latitude is not set correctly!")
                if (imageList.isEmpty()) errorList.add("No image provided or unallowed format!")


                if (errorList.isEmpty()) {
                    dcList.add(BiocollectBiom(
                        serial,
                        surveyDate!!,
                        recordedBy,
                        latitude!!,
                        longitude!!,
                        species,
                        scientificName!!,
                        count!!.toInt(),
                        comment,
                        imageList
                    ))
                } else {
                    val err = "Error in <Serial: $serial>: " + errorList.joinToString(", ")
                    logger?.error(err)
                }

            }
        }
    }

    fun convDate(str: String): LocalDate? {
        try {
            return LocalDate.parse(str, formatter)
        } catch (ex: DateTimeParseException) {
            return null
        }
    }

    fun convDate2(str: String): LocalDateTime? {
        try {
            return LocalDateTime.parse(str, formatter2)
        } catch (ex: DateTimeParseException) {
            return null
        }
    }


    fun convCount(str: String): Int? {
        try {
            if (str.startsWith(">"))
                return str.split(">")[1].toInt()
            else
                return str.split(" ")[0].toInt()
        }
        catch (ex: Exception) {
            return null
        }


    }

}


