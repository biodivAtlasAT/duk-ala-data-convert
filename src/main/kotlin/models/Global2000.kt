package duk.at.models

import duk.at.*
import org.apache.logging.log4j.LogManager
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.LocalDateTime


class Global2000(private val cli: Cli){
    /* Global200 sends an xls file which has already the structure of the survey
    a few columns have to be added
     */

    companion object {
        private val logger: org.apache.logging.log4j.Logger? = LogManager.getLogger()
    }
    val dcList = mutableListOf<BiocollectBiom>()

    fun convert() {
        val startLine = 2
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
                var comment = ""
                var sightingDate: LocalDateTime? = null
                var surveyDate: LocalDate? = null
                var imageList: MutableList<Image> = mutableListOf()
                var serial: String = ""
                var scientificName: String? = null
                var identificationRemarks: String? = null
                var identificationConfidence1 = "uncertain"
                var errorList: MutableList<String> = mutableListOf()
                var defaultScientificName: String = ""

                var cntArray = IntArray(6)

                /* serial	surveyDate	surveyStartTime	notes	recordedBy	location	locationLatitude	locationLongitude
                species1.name	species1.scientificName	species1.commonName	species1.guid	individualCount1
                identificationConfidence1	comments1
                sightingPhoto1.url	sightingPhoto1.licence	sightingPhoto1.name	sightingPhoto1.filename	sightingPhoto1.attribution	sightingPhoto1.notes	sightingPhoto1.projectId    sightingPhoto1.projectName	sightingPhoto1.dateTaken
                sightingPhoto2.url	sightingPhoto2.licence	sightingPhoto2.name	sightingPhoto2.filename	sightingPhoto2.attribution	sightingPhoto2.notes	sightingPhoto2.projectId	sightingPhoto2.projectName	sightingPhoto2.dateTaken
                sightingPhoto3.url	sightingPhoto3.licence	sightingPhoto3.name	sightingPhoto3.filename	sightingPhoto3.attribution	sightingPhoto3.notes	sightingPhoto3.projectId	sightingPhoto3.projectName	sightingPhoto3.dateTaken
                sightingPhoto4.url	sightingPhoto4.licence	sightingPhoto4.name	sightingPhoto4.filename	sightingPhoto4.attribution	sightingPhoto4.notes	sightingPhoto4.projectId	sightingPhoto4.projectName	sightingPhoto4.dateTaken
                project_name	collectionID	occurrenceID	catalogNumber	fieldNumber	identificationRemarks	occurrenceStatus	basisOfRecord	institutionCode	collectionCode	phylum	class */

                for (cell in row) {
                    imageList.clear()
                    if (cell.columnIndex == 0) serial = cell.makeIntFromStringOrNumeric.toString()
//                    if (cell.columnIndex == 1) surveyDate = errorList.AddWhenNull(cell.makeDateStringFromString("dd.MM.yyyy", true), "TIMESTAMP is incorrect!")
                    //if (cell.columnIndex == 3) notes = cell.stringCellValue
                    if (cell.columnIndex == 4) recordedBy = errorList.AddWhenEmpty(cell.stringCellValue, "Observer is empty!")
                    if (cell.columnIndex == 6) longitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2,"Longitude is incorrect!")
                    if (cell.columnIndex == 7) latitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2, "Latitude is incorrect!")
                    //if (cell.columnIndex == 8) name = cell.stringCellValue
                    if (cell.columnIndex == 9) {
                        species = cell.stringCellValue
                        if (species.isEmpty())
                            errorList.add("column ART/Species is empty!")
                      /*  else
                            scientificName = errorList.AddWhenNull(cell.getScientificName(cli, defaultScientificName),"scientificName for <$species/$defaultScientificName> not found in BIE or not in LIST!")*/
                    }
                    //if (cell.columnIndex == 10) commonName = cell.stringCellValue
                    if (cell.columnIndex == 12) count = cell.numericCellValue.toInt()
                    if (cell.columnIndex == 14) comment = cell.stringCellValue

                    // --> imageList

                    if (cell.columnIndex == 51) projectName = cell.stringCellValue
                    if (cell.columnIndex == 56) identificationRemarks = if (cell.stringCellValue == null) "" else cell.stringCellValue

                }

                if (errorList.size == 0) {
               /*     dcList.add(BiocollectBiom(
                        serial,
                        surveyDate!!,
                        recordedBy,
                        latitude!!,
                        longitude!!,
                        species!!,
                        scientificName!!,
                        count!!,
                        comment,
                        imageList,
                        identificationRemarks!!,
                        identificationConfidence1,
                        projectName
                    ))*/
                } else {
                    val err = "Error in Row: ${row.rowNum+1}: " + errorList.joinToString(", ")
                    logger?.error(err)
                }
            }

            file.close()
            workbook.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


