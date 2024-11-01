package duk.at.services

data class ListsItem(
    val id: Int,
    val name: String,
    val commonName: String?,
    val scientificName: String?,
    val lsid: String?,
    val dataResourceUid: String
)
