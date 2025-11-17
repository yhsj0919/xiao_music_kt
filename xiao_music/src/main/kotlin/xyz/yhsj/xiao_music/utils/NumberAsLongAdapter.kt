package xyz.yhsj.xiao_music.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

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