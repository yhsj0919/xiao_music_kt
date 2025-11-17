package xyz.yhsj.server.plugins

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*


fun Application.configureSerialization() {
    //序列化
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)


            serializerProvider.setNullKeySerializer(object : JsonSerializer<Any>() {
                override fun serialize(value: Any, gen: JsonGenerator, serializers: SerializerProvider) {
                    gen.writeFieldName("")
                }
            })
        }
    }
}

