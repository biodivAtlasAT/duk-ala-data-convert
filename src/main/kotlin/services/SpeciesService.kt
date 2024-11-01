package duk.at.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URLEncoder

class SpeciesService private constructor () {
    companion object {
        private val sL: MutableList<ListsItem> = mutableListOf()
        private val instance: SpeciesService by lazy { SpeciesService() }

        fun getInstance(drs: List<String>): SpeciesService {
            drs.forEach {
                sL.addAll(getSpeciesList(it))
            }
            sL.distinct()
            return instance
        }

        private fun getSpeciesList(dr: String): List<ListsItem> {
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://lists.biodivdev.at/ws/speciesListItems/$dr"))
                .GET()
                .build()
            val body = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
            val objectMapper = ObjectMapper().registerKotlinModule()
            val speciesList: List<ListsItem> = objectMapper.readValue(body, object : TypeReference<List<ListsItem>>() {})
            return speciesList
        }

    }

    fun getSpecies(name: String): String? {
        val bieList = getSpeciesFromBie(name)
        bieList.forEach { it1 ->
            if (sL.count { it1.acceptedName == it.scientificName } > 0)
                return it1.acceptedName
        }
        return null
    }

    private fun getSpeciesFromBie(name: String): List<BieItem> {
        val encodedPara = URLEncoder.encode(name, "UTF-8")
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://bie.biodivdev.at/ws/guid/$encodedPara"))
            .GET()
            .build()

        val body = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
        val objectMapper = ObjectMapper().registerKotlinModule()
        try {
            val bieList: List<BieItem> = objectMapper.readValue(body, object : TypeReference<List<BieItem>>() {})
            return bieList
        }
        catch (ex: MismatchedInputException) {
            return emptyList()
        }

    }


}