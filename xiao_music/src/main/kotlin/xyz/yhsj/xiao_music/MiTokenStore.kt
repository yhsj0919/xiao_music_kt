package xyz.yhsj.xiao_music

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter

class MiTokenStore(private val tokenPath: String) {
    fun loadToken(): MutableMap<String?, Any?>? {
        val f = File(tokenPath)
        if (f.isFile()) {
            try {
                return gson.fromJson( f.readText(Charsets.UTF_8), MutableMap::class.java) as MutableMap<String?, Any?>?
            } catch (e: Exception) {
                LOGGER.error("Exception on load token from {}", tokenPath, e)
            }
        }
        return null
    }

    fun saveToken(token: MutableMap<String?, Any?>?) {
        try {
            FileWriter(tokenPath).use { writer ->
                gson.toJson(token, writer)
            }
        } catch (e: Exception) {
            LOGGER.error("Exception on save token to {}", tokenPath, e)
        }
    }

    fun deleteToken() {
        val f = File(tokenPath)
        if (f.exists()) f.delete()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MiTokenStore::class.java)
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    }
}
