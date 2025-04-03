package duk.at.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import duk.at.Cli
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object CollectoryService {

    fun checkProviderMap(cli: Cli): Boolean {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${cli.collectoryUrl}/lookup/inst/${cli.instCode}/coll/${cli.collCode}"))
            .GET()
            .build()
        val body = client.send(request, HttpResponse.BodyHandlers.ofString()).body()

        val mapper = jacksonObjectMapper()
        val result: Map<String, Any> = mapper.readValue(body.toString(), object : TypeReference<Map<String, Any>>() {})
        return !result.containsKey("error")
    }
}