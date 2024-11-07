package duk.at

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import duk.at.models.Biom
import duk.at.services.SpeciesService
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import java.text.SimpleDateFormat
import java.util.*

class Cli  : CliktCommand(){
    val verbose by option("-v", "--verbose", help="Show Details").flag()
    val ifile by option(help="Name of the input file").required()
    val ofile by option(help="Name of the output file").required()
    val imodel by option(help="Name of the input file model").choice("BIOM", "ATIV").required()
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

fun Cell.makeDateStringFromString(simpleDateFormat: String): String? {
    if (this.cellType != CellType.STRING) {
        if (DateUtil.isCellDateFormatted(this)) {
            val date: Date = this.dateCellValue
            val sdf = SimpleDateFormat(simpleDateFormat)
            return sdf.format(date)
        }
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
        if (longlat?.subSequence(0,4) == "0.00")
            longlat = null
        if (longlat?.subSequence(0,4) == "0.00")
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