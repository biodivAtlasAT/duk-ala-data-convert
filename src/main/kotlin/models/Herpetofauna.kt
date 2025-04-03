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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Herpetofauna(private val cli: Cli){
    /*IDPerson,Name,Nachname,Trust_Status,E-Mail,ID_Fund,Jahr_Meldung,Zeige_YN,Jahr_Fund,
    Monat_Fund,Tag_Fund,Wetter,Bundesland,Bezirk,Ort,Anmerkung,Laenge_geograph,Breite_Geograph,m.ü.A.,
    source_m.ü.A.,Beschreibung_Fundort,Fund_Art_ID,Wanderung,Name_deutsch,Gattung_wiss,Art_wiss,Gruppe,
    Anz_Ei,Anz_Larv,Anz_juv,Anz_adult,Anz_ruf,Art_Bemerkung,Bestaetigt,Bestaetigt_ vonwem,Bestaetigt_anmerkung,
    Bild_1_Dateiname,BIOMGarten_JN
     */

    companion object {
        private val logger: org.apache.logging.log4j.Logger? = LogManager.getLogger()
    }
    val dcList = mutableListOf<BiocollectBiom>()

    fun convert() {

        try {
            val file = FileInputStream(File(cli.ifile))
            val workbook: Workbook = XSSFWorkbook(file)
            val sheet: Sheet = workbook.getSheetAt(0)

            for (row in sheet) {
                if (row.rowNum < 1) continue
                if (row.rowNum > cli.count + 1) break

                var species: String? = null
                var projectName: String? = null
                var count: Int? = null
                var recordedBy: String = ""
                var longitude: Double? = null
                var latitude: Double? = null
                var comment = ""
                var sightingDate: LocalDateTime? = null
                var surveyDate: LocalDate? = null
                var imageList: MutableList<Image> = mutableListOf()
                var serial: String = row.rowNum.toString()
                var scientificName: String? = null
                var identificationRemarks: String? = null
                var errorList: MutableList<String> = mutableListOf()
                var surveyYYYY: Int? = null
                var surveyMM: Int? = null
                var surveydd: Int? = null

                var cntArray = IntArray(5)

                for (cell in row) {
                    imageList.clear()


                    if (cell.columnIndex == 1) recordedBy = cell.stringCellValue
                    if (cell.columnIndex == 2) recordedBy = recordedBy + " " + cell.stringCellValue
                    if (cell.columnIndex == 8) surveyYYYY = cell.numericCellValue.toInt()
                    if (cell.columnIndex == 9) surveyMM = cell.numericCellValue.toInt()
                    if (cell.columnIndex == 10) surveydd = cell.numericCellValue.toInt()
                    if (cell.columnIndex == 10) {
                        val dateString =
                            "${surveyYYYY}-${String.format("%02d", surveyMM)}-${String.format("%02d", surveydd)}"
                        val formatter = DateTimeFormatter.ISO_DATE
                        surveyDate = LocalDate.parse(dateString, formatter)
                    }

                    if (cell.columnIndex == 15) comment = cell.stringCellValue
                    if (cell.columnIndex == 16) longitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2,"Longitude is incorrect!")
                    if (cell.columnIndex == 17) latitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2, "Latitude is incorrect!")
                    if (cell.columnIndex == 23) {
                        species = cell.stringCellValue
                        if (species.isEmpty())
                            errorList.add("column ART is empty!")
                      /*  else
                            scientificName = errorList.AddWhenNull(cell.getScientificName(cli),"scientificName for <$species> not found in BIE or not in LIST!")
                            */

                    }
                    if (cell.columnIndex in 27..31) cntArray[cell.columnIndex-27] = cell.numericCellValue.toInt()
                    if (cell.columnIndex == 32) identificationRemarks = if (cell.stringCellValue == null) "" else cell.stringCellValue
                    val localDate = LocalDate.parse("2024-02-15")
                    if (cell.columnIndex == 36)
                            imageList = ImageList(cell.stringCellValue, recordedBy, "CC BY 3.0", surveyDate!!.atStartOfDay(), errorList).iL

/*
                    if (cell.columnIndex == 1) projectName = cell.stringCellValue
                    if (cell.columnIndex == 4) {
                        species = cell.stringCellValue
                        if (species.isEmpty())
                            errorList.add("column ART is empty!")
                        else
                            scientificName = errorList.AddWhenNull(cell.getScientificName(cli),"scientificName for <$species> not found in BIE or not in LIST!")
                    }
                    if (cell.columnIndex in 9..17) cntArray[cell.columnIndex-9] = cell.numericCellValue.toInt()
                    if (cell.columnIndex == 22) comment = cell.stringCellValue
                    if (cell.columnIndex == 31) identificationRemarks = if (cell.stringCellValue == null) "" else cell.stringCellValue
                    if (cell.columnIndex == 32) serial = cell.makeStringFromStringOrNumeric
                    if (cell.columnIndex == 33) surveyDate = errorList.AddWhenNull(cell.makeDateStringFromString("yyyyMMdd"), "TIMESTAMP is incorrect!")
                    if (cell.columnIndex == 37) recordedBy = errorList.AddWhenEmpty(cell.stringCellValue, "Obeserver is empty!")
                    if (cell.columnIndex == 64) longitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2,"Longitude is incorrect!")
                    if (cell.columnIndex == 65) latitude = errorList.AddWhenNull(cell.makeLongLatStringFromStringOrNumeric2, "Latitude is incorrect!")
                    if (cell.columnIndex == 78) sightingDate = errorList.AddWhenNull(cell.makeDateTimeStringFromString("yyyy-MM-dd HH:mm:ss.S"), "DATETIMEORIGINAL is incorrect!")

                    // if (cell.columnIndex == 77) imageList = ImageList(cell.stringCellValue, recordedBy, "CC BY 3.0", sightingDate, errorList).iL
*/
                }
                count = cntArray.sum()

                println("${recordedBy} - ${surveyDate}")

                if (imageList.size == 0)
                    errorList.add("No valid Image found!")
                if (errorList.size == 0) {
             /*       dcList.add(BiocollectBiom(
                        serial,
                        surveyDate!!,
                        recordedBy,
                        latitude!!,
                        longitude!!,
                        species!!,
                        scientificName!!,
                        count!!.toInt(),
                        comment,
                        imageList,
                        identificationRemarks ?: ""
                    ))*/
                } else {
                    val err = "Error in RowNum: ${row.rowNum+1} " + errorList.joinToString(", ")
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


