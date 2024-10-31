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
    val sightingPhoto1Url: String = "",
    val sightingPhoto1Licence: String = "",
    val sightingPhoto1Name: String = "",
    val sightingPhoto1Filename: String = "",
    val sightingPhoto1DateTaken: String = "",
    val projectName: String = "",
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
