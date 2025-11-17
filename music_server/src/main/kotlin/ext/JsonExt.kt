package xyz.yhsj.server.ext

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.text.DateFormat


var jackson = ObjectMapper()


val gson: Gson =
    GsonBuilder().setDateFormat(DateFormat.LONG).setPrettyPrinting().excludeFieldsWithModifiers(Modifier.STATIC)
        .create()


fun Any?.json(compact: Boolean = true): String {
    if (this == null) return ""

    return if (compact) {
        // 压缩 JSON（minify）
        jackson.writeValueAsString(this)

    } else {
        // 美化输出（pretty print）

        jackson.writerWithDefaultPrettyPrinter()
            .writeValueAsString(this)
    }
}


//json反序列化
inline fun <reified T> fromJson(json: String): T {
    return jackson.readValue(json, T::class.java)

}

inline fun <reified T> String?.toModel(): T {
    return jackson.readValue(this, object : TypeReference<T>() {})
}

inline fun <reified T> Any?.toModel(): T {
    return if (this is String) {
        jackson.readValue(this, object : TypeReference<T>() {})
    } else {
        jackson.readValue(this.json(), object : TypeReference<T>() {})
    }

}


//json序列化
fun Any?.gson(): String = if (this != null) {
    gson.toJson(this)
} else {
    ""
}

//json反序列化
inline fun <reified T> fromGson(json: String): T {

    val type: Type = object : TypeToken<T>() {}.type

    return gson.fromJson(json, type)
}

inline fun <reified T> String?.gtoModel(): T {

    val type: Type = object : TypeToken<T>() {}.type

    return gson.fromJson(this, type)
}