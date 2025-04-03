package duk.at.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class BiocollectBiom(
    val serial: String,
    val surveyDate: LocalDate,
    val surveyStartTime: LocalTime?,
    val notes: String,
    val recordedBy: String,
    val location: String?,
    val locationLatitude: Double,
    val locationLongitude: Double,
    val species1Name: String,
    val species1ScientificName: String,
    val species1CommonName: String,
    val species1Guid: String,
    val individualCount1: Int,
    val comments: String,
    val institutionCode: String,
    val collectionCode: String,
    val imageList: MutableList<Image>
) {

}

data class ImageList(val str: String, val recordedBy: String, val license: String, val dateTaken: LocalDateTime?, val errorList: MutableList<String>) {
    val iL: MutableList<Image> = mutableListOf()
    private val validExt: List<String> = listOf("png", "jpeg", "jpg", "gif")
    init {
        str.split(",").filter { it.trim().isNotEmpty() }.forEach {
            val img = Image(it.trim(), recordedBy, license, dateTaken)
            if (validExt.contains(img.fileExtension) )
                iL.add(Image(it.trim(), recordedBy, license, dateTaken))
            else
                errorList.add("File extension ${img.fileExtension} of image is incorrect!")
        }
    }
}

data class Image(val str: String, val attribution: String, val license: String, val dateTaken: LocalDateTime?) {
    var url: String = str
    var name: String
    var fileName: String
    val notes: String = ""
    val projectId: String = ""
    val projectName: String = ""
    var fileExtension: String = ""

    init {
        val parts = str.split("/")
        fileName = parts[parts.size - 1]
        name = fileName
        val arr = name.split(".")
        fileExtension = arr[arr.size - 1]
    }
}