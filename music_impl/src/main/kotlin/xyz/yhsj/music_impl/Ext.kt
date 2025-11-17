package xyz.yhsj.music_impl


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Random

val gson: Gson =
    GsonBuilder().registerTypeAdapter(Map::class.java, NumberAsLongAdapter()).setPrettyPrinting().excludeFieldsWithModifiers(Modifier.STATIC)
        .create()


//json序列化
fun Any?.json(): String = if (this != null) {
    gson.toJson(this)
} else {
    ""
}

//json反序列化
inline fun <reified T> fromGson(json: String): T {

    val type: Type = object : TypeToken<T>() {}.type

    return gson.fromJson(json, type)
}

inline fun <reified T> String?.toModel(): T {

    val type: Type = object : TypeToken<T>() {}.type

    return gson.fromJson(this, type)
}

fun String?.toMap(): Map<String, Any> {
    val type = object : TypeToken<Map<String, Any>>() {}.type
    return gson.fromJson(this, type)
}


fun getRandomString(length: Int): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val sb = StringBuilder(length)
    val r = Random()
    for (i in 0..<length) sb.append(chars[r.nextInt(chars.length)])
    return sb.toString()
}

fun sha1(s: String): ByteArray {
    val md = MessageDigest.getInstance("SHA-1")
    return md.digest(s.toByteArray(StandardCharsets.UTF_8))
}

fun md5(s: String): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.toByteArray(StandardCharsets.UTF_8))
    val sb = StringBuilder()
    for (b in digest) sb.append(String.format("%02x", b))
    return sb.toString()
}

fun ByteArray.hex(): String {

    return this.joinToString("") { "%02x".format(it) }
}

class NumberAsLongAdapter : JsonDeserializer<MutableMap<String?, Any?>?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MutableMap<String?, Any?> {
        val map: MutableMap<String?, Any?> = LinkedHashMap()
        val obj = json.getAsJsonObject()
        for (entry in obj.entrySet()) {
            val e = entry.value
            if (e.isJsonPrimitive) {
                val p = e.getAsJsonPrimitive()
                if (p.isNumber) {
                    try {
                        val l = p.asLong
                        map[entry.key] = l
                    } catch (ex: NumberFormatException) {
                        map[entry.key] = p.asDouble
                    }
                } else if (p.isBoolean) {
                    map[entry.key] = p.getAsBoolean()
                } else {
                    map[entry.key] = p.getAsString()
                }
            } else if (e.isJsonObject) {
                map[entry.key] = deserialize(e, typeOfT, context)
            } else if (e.isJsonArray) {
                map[entry.key] = e.getAsJsonArray()
            } else {
                map[entry.key] = null
            }
        }
        return map
    }
}


fun String.encodeUrl(): String {
    val parts = this.split("/")

    return parts.mapIndexed { index, part ->
        if (index < 3) {
            part
        } else {
            if (isEncoded(part)) part
            else URLEncoder.encode(part, "UTF-8").replace("+", "%20")
        }
    }.joinToString("/")
}

private fun isEncoded(text: String): Boolean {
    return Regex("%[0-9A-Fa-f]{2}").containsMatchIn(text)
}