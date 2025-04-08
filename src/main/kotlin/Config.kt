package duk.at

import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.util.Properties

object Config {
    private val properties: Properties = Properties()
    private var configFileName = ""
    var notFound = false

    init {
        try {
            val file = File(ConfigFileName.cfg)
            //val file = File("config.properties")
            val inputStream: InputStream = file.inputStream()
            properties.load(inputStream)
        } catch (e: Exception) {
            println("Config file not found --> Exit!")
            notFound = true
        }
    }

    fun getProperty(key: String): String? {
        return properties.getProperty(key) ?: null
    }
}

class ConfigFileName private constructor(val parameter: String) {
    companion object {
        var cfg = ""
        fun create(param: String): ConfigFileName {
            cfg = param
            return ConfigFileName(param)
        }
    }
}

