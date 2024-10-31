package duk.at

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import duk.at.models.BiocollectBiom
import duk.at.models.Biom

class Cli  : CliktCommand(){
    val verbose by option("-v", "--verbose", help="Show Details").flag()
    val ifile by option(help="Name of the input file").required()
    val ofile by option(help="Name of the output file").required()
    val imodel by option(help="Name of the input file model").choice("BIOM", "ATIV").required()

    override fun run() {
        if (verbose) {
            echo("Verarbeitete die Datei: ${ifile}")
        }
        if (imodel == "BIOM") {
            val biom = Biom(ifile, ofile)
            val l = biom.convert()
            println(l.size)
            biom.createWorkbook(l)
        }
    }

}