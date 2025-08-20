package duk.at.models

import duk.at.*
import duk.at.models.Artenzaehlen.Companion
import duk.at.services.SpeciesService
import org.apache.logging.log4j.LogManager
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.LocalDateTime


class Global2000(private val cli: Cli){

    companion object {
        private val logger: org.apache.logging.log4j.Logger? = LogManager.getLogger()
    }
    val dcList = mutableListOf<BiocollectBiom>()

    fun convert() {
        /* ID	GARTEN_ID	ADRESSE_1	ADRESSE_2	ART_VAL	quality	lat_final	lon_final	uncert
        file1	file2	file3	file4	file5	file6	file7	file8	DATUM */
        val startLine = 1
        try {
            val file = FileInputStream(File(cli.ifile))
            val workbook: Workbook = XSSFWorkbook(file)
            val sheet: Sheet = workbook.getSheetAt(0)

            for (row in sheet) {
                if (row.rowNum < startLine) continue
                if (row.rowNum > cli.count + startLine-1) break

                var species: String? = null
                var projectName: String = ""
                var count: Int? = null
                var recordedBy: String = ""
                var longitude: Double? = null
                var latitude: Double? = null
                var notes = ""
                var sightingDate: LocalDateTime? = null
                var surveyDate: LocalDate? = null
                var imageList: MutableList<Image> = mutableListOf()
                var serial: String = ""
                var scientificName: String? = null
                var errorList: MutableList<String> = mutableListOf()
                var defaultScientificName: String = ""
                var guid: String = ""

                imageList.clear()

                for (cell in row) {
                    if (cell.columnIndex == 0) serial = cell.makeStringFromStringOrNumeric
                    if (cell.columnIndex == 4) {
                        species = cell.stringCellValue
                        defaultScientificName = species
                        if (species.isEmpty())
                            errorList.add("column ART/Species is empty!")
                        else {
                            val foundSpecies = SpeciesService.getInstance(
                                cli.speciesLists.split(",").toList(),
                                cli.listsUrl
                            ).getSpecies(cell.stringCellValue, cli.bieUrl, defaultScientificName)

                            if (foundSpecies == null) {
                                errorList.AddWhenNull(foundSpecies as String?,
                                    "scientificName for <$species/$defaultScientificName> not found in BIE or not in LIST!"
                                )
                            } else {
                                scientificName = foundSpecies.name
                                guid = foundSpecies.identifier
                            }
                        }
                    }
                    if (cell.columnIndex == 6) latitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2, "Latitude is incorrect!")
                    if (cell.columnIndex == 7) longitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2,"Longitude is incorrect!")
                    if (cell.columnIndex == 9 && cell.stringCellValue != "") imageList.add(Image(cell.stringCellValue))
                    if (cell.columnIndex == 10 && cell.stringCellValue != "") imageList.add(Image(cell.stringCellValue))
                    if (cell.columnIndex == 11 && cell.stringCellValue != "") imageList.add(Image(cell.stringCellValue))
                    if (cell.columnIndex == 12 && cell.stringCellValue != "") imageList.add(Image(cell.stringCellValue))
                    if (cell.columnIndex == 17) surveyDate = errorList.AddWhenNull(cell.makeDateStringFromString("yyyy-MM-dd"), "TIMESTAMP is incorrect!")

                }

                if (surveyDate == null) {
                    errorList.add("TIMESTAMP is empty!")
                }

                if (errorList.size == 0) {
                    dcList.add(BiocollectBiom(
                        serial,
                        surveyDate!!,
                        null,
                        notes,
                        recordedBy,
                        null,
                        latitude!!,
                        longitude!!,
                        species!!,
                        scientificName!!,
                        species,
                        guid,
                        1,
                        "",
                        cli.instCode,
                        cli.collCode,
                        imageList
                    ))
                } else {
                    val err = "Error in Row: ${row.rowNum+1}: " + errorList.joinToString(", ")
                    Global2000.logger?.error(err)
                }
            }

            file.close()
            workbook.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


