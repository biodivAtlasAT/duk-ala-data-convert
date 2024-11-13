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


class Naturbeobachtung(private val cli: Cli){
    /*PROJECT_ID	PROJTITLE	FULLNAME	AUTOREN	TRIVIALNAME	OTHERTAXON	PTNAME_ID	VALIDATOR	NO_IDENT_SEX	MALE
    FEMALE	PUPPEN	RAUPEN	EIER	TOT_NO_IDENT	TOT_MALE	TOT_FEMALE	ADULTSUM	CONDITION	DATASOURCE
    IMAGE_CNT	VERHALTEN	COMMENTARY	TRACESDEF	TRACES1	TRACES2	TRACES3	TRACES4	TRACES5	TRACES6	QS_STATUS
    QS_COMMENT	UUID	ERFASST_AM	GATHDATA_ID	TOTAL_COMMENTS	TOTAL_LIKES	OBSERVERS	DATATYPIST	USER_ID
    USERHASH	GATHEVENT_ID	DATE_FROM	DATE_TO	TIME_FROM	TIME_TO	IS_NULLEVENT	BEWOELKUNG	WEATHER	WIND
    TEMPERATUR	EVENT_REMARKS	TEXT_DATUM	SORT_DATUM	RECORDYEAR	ICC	ICCLEVEL2	REGION	ZIPCODE	CITY	LOCATION
    LOCATIONTEXT	ALTITUDE_FROM	ALTITUDE_TO	LON_DECDEG	LAT_DECDEG	COORD_METHOD	LON_DEGSTRING	LAT_DEGSTRING
    COORD_BLUR	GEOREF_METHODE	COORD_MAKER	MARKED_FUZZY	TCODE	LOCATION_ID	IMAGE_ID	IMAGE_ORIG	IMGFILENAME_ORIG
    DATETIMEORIGINAL	GMT	GPS_LAT	GPS_LONG	GPS_ACCURACY	GPS_ALTITUDE	DATA_LINK
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
                if (row.rowNum < 6) continue
                if (row.rowNum > cli.count + 6) break

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
                var serial: String = ""
                var scientificName: String? = null
                var identificationRemarks: String? = null
                var errorList: MutableList<String> = mutableListOf()

                var cntArray = IntArray(9)

                for (cell in row) {
                    imageList.clear()
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

                    // TODO: Achtung sightingDate kommt nach dem Image
                    // TODO: URL muss nich von extern kommen
                    // TODO projetName gehört zu Image
                    // if (cell.columnIndex == 77) imageList = ImageList(cell.stringCellValue, recordedBy, "CC BY 3.0", sightingDate, errorList).iL

                }
                count = cntArray.sum()

        /*        if (imageList.size == 0)
                    errorList.add("No valid Image found!")*/
                if (errorList.size == 0) {
                    dcList.add(BiocollectBiom(
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
                        identificationRemarks!!
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
    }

}


