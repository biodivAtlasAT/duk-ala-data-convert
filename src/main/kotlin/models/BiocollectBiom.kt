package duk.at.models

data class BiocollectBiom(
    val serial: String,
    val surveyDate: String,
    val recordedBy: String,
    val locationLatitude: String,
    val locationLongitude: String,
    val species1Name: String,
    val species1ScientificName: String,
    val individualCount1: Int,
    val comments1: String,
    val imageList: MutableList<Image>,
    val collectionID: String = "",
    val occurrenceID: String = "",
    val catalogNumber: String = "",
    val fieldNumber: String = "",
    val identificationRemarks: String = "",
    val occurrenceStatus: String = "",
    val basisOfRecord: String = "",
    val phylum: String = "",
    val clazz: String = "",
) {

}

data class ImageList(val str: String, val recordedBy: String, val license: String, val dateTaken: String?) {
    val iL: MutableList<Image> = mutableListOf()
    init {
        str.split(",").filter { it.trim().isNotEmpty() }.forEach {
            iL.add(Image(it.trim(), recordedBy, license, dateTaken))
        }
    }
}

data class Image(val str: String, val attribution: String, val license: String, val dateTaken: String?) {
    var url: String = str
    var name: String
    var fileName: String
    val notes: String = ""
    val projectId: String = ""
    val projectName: String = ""

    init {
        val parts = str.split("/")
        fileName = parts[parts.size - 1]
        name = fileName
    }
}