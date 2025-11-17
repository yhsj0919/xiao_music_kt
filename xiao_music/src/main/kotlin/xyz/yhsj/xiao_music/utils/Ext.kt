package xyz.yhsj.xiao_music.utils


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import xyz.yhsj.xiao_music.NumberAsLongAdapter
import java.lang.reflect.Modifier
import java.lang.reflect.Type
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