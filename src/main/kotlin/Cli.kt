package duk.at

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import duk.at.models.Artenzaehlen
import duk.at.models.Biom
import duk.at.models.Naturbeobachtung
import duk.at.services.SpeciesService
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DateUtil
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Cli  : CliktCommand(){
    val verbose by option("-v", "--verbose", help="Show Details").flag()
    val ifile by option(help="Name of the input file").required()
    val ofile by option(help="Name of the output file").required()
    val template by option(help="Name of the template .xlsx-file for bulk upload").required()
    val imodel by option(help="Name of the input file model").choice("BIOM", "ATIV", "ARTENZAEHLEN", "NATURBEOBACHTUNG").required()
    val speciesLists by option(help="Names of the used data resources of the lists application").required()
    val count by option(help="Count of rows to transform").int().default(Int.MAX_VALUE)
    val listsUrl by option(help="URL of the lists tool: e.g.: https://lists.biodivdev.at/ws/speciesListItems").required()
    val bieUrl by option(help="URL of the bie tool: e.g. https://bie.biodivdev.at/ws/guid").required()

    override fun run() {
        if (verbose) {
            echo("Verarbeitete die Datei: ${ifile}")
        }
       if (imodel == "BIOM") {
            val biom = Biom(this)
            val l = biom.convert()
            println(l.size)
            biom.createWorkbook(l)
        }
        if (imodel == "ARTENZAEHLEN") {
            val model = Artenzaehlen(this)
            model.convertCSV()
            println("# of records: ${model.dcList.size}")
            BiocollectBiomList.createWorkbook(model.dcList, this)
        }
        if (imodel == "NATURBEOBACHTUNG") {
            val model = Naturbeobachtung(this)
            model.convert()
            println("# of records: ${model.dcList.size}")
            BiocollectBiomList.createWorkbook(model.dcList, this)
        }



    }

}

fun <String> MutableList<String>.AddWhenEmpty(str: String, msg: String): String {
    if (str == "")
        this.add(msg)
    return str
}

fun <String> MutableList<String>.AddWhenNull(num: Int?, msg: String): Int? {
    if (num == null)
        this.add(msg)
    return num
}
fun <String> MutableList<String>.AddWhenNull(str: String?, msg: String): String? {
    if (str == null)
        this.add(msg)
    return str
}

fun <String> MutableList<String>.AddWhenNull(str: LocalDate?, msg: String): LocalDate? {
    if (str == null)
        this.add(msg)
    return str
}

fun <String> MutableList<String>.AddWhenNull(str: LocalDateTime?, msg: String): LocalDateTime? {
    if (str == null)
        this.add(msg)
    return str
}


fun <String> MutableList<String>.AddWhenNull(str: Double?, msg: String): Double? {
    if (str == null)
        this.add(msg)
    return str
}


fun Cell.makeDateStringFromString(simpleDateFormat: String): LocalDate? {
    if (this.cellType != CellType.STRING) {
        if (DateUtil.isCellDateFormatted(this)) {
            val date: Date = this.dateCellValue
            val sdf = SimpleDateFormat(simpleDateFormat)
            val dateTimeString = sdf.format(date)
            val formatter = DateTimeFormatter.ISO_DATE
            val dateTime = LocalDate.parse(dateTimeString, formatter)
            return dateTime
        }
    }
    if (this.cellType == CellType.STRING) {
        val formatter = DateTimeFormatter.ofPattern(simpleDateFormat)
        val localDate = LocalDate.parse(this.stringCellValue, formatter)
        return localDate
    }
    return null
}

fun Cell.makeDateTimeStringFromString(simpleDateFormat: String): LocalDateTime? {
   /* if (this.cellType != CellType.STRING) {
        if (DateUtil.isCellDateFormatted(this)) {
            val date: Date = this.dateCellValue
            val sdf = SimpleDateFormat(simpleDateFormat)
            val dateTimeString = sdf.format(date)
            val formatter = DateTimeFormatter.ISO_DATE
            val dateTime = LocalDate.parse(dateTimeString, formatter)
            return dateTime
        }
    }*/
    if (this.cellType == CellType.STRING) {
        val targetSize = simpleDateFormat.length
        val str = this.stringCellValue.subSequence(0,targetSize)

        val formatter = DateTimeFormatter.ofPattern(simpleDateFormat)
        val localDate = LocalDateTime.parse(str, formatter)
        return localDate
    }
    return null
}



fun Cell.getScientificName(cli: Cli): String? = SpeciesService.getInstance(
    cli.speciesLists.split(",").toList(),
    cli.listsUrl
).getSpecies(this.stringCellValue, cli.bieUrl)

val Cell.makeLongLatStringFromStringOrNumeric: String?
    get() {
        var longlat: String? = null
        if (this.cellType == CellType.NUMERIC)
            longlat = this.numericCellValue.toString()
        if (this.cellType == CellType.STRING)
            longlat = this.stringCellValue
        if (longlat?.subSequence(0,1) == "0")
            longlat = null
        return longlat
    }

val Cell.makeLongLatStringFromStringOrNumeric2: Double?
    get() {
        var longlat: Double? = null

        if (this.cellType == CellType.NUMERIC) {
            longlat = this.numericCellValue
        }
        if (this.cellType == CellType.STRING) {
            //val h = this.stringCellValue.replace(".",",")
            val h = this.stringCellValue
            longlat = h.toDouble()
        }
        val h1 = longlat.toString()
        if (h1.subSequence(0,3) == "0.0")
            longlat = null
        return longlat
    }


val Cell.makeIntFromStringOrNumeric: Int?
    get() {
        var rc: Int? = null
        if (this.cellType == CellType.NUMERIC)
            rc = this.numericCellValue.toInt()
        if (this.cellType == CellType.STRING) {
            if (this.stringCellValue.startsWith(">"))
                rc = this.stringCellValue.split(">")[1].toInt()
            else
                rc = this.stringCellValue.split(" ")[0].toInt()
        }
        return rc
    }

val Cell.makeStringFromStringOrNumeric: String
    get() {
        var rc: String = ""
        if (this.cellType == CellType.NUMERIC)
            rc = this.numericCellValue.toInt().toString()
        if (this.cellType == CellType.STRING)
            rc = this.stringCellValue
        return rc
    }