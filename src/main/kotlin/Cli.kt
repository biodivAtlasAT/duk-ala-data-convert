package duk.at

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import duk.at.models.Biom

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
       /* val name = SpeciesService.getInstance(speciesLists).getSpecies("Dice Snake")
        val name1 = SpeciesService.getInstance(speciesLists).getSpecies("Äskulapnatter")
        val name2 = SpeciesService.getInstance(speciesLists).getSpecies("Nixda")
        println("Scientific Name: $name")
        println("Scientific Name: $name1")
        println("Scientific Name: $name2")*/


    }

}