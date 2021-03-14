package io.github.starwishsama.comet.utils.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.starwishsama.comet.objects.wrapper.*
import java.io.InvalidObjectException
import java.text.SimpleDateFormat
import java.time.LocalDateTime

object WrapperConverter: JsonDeserializer<WrapperElement>() {
    private val noLoopMapper: ObjectMapper = ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerKotlinModule()
        .registerModule(SimpleModule().also {
            it.addDeserializer(LocalDateTime::class.java, LocalDateTimeConverter)
        })
        .setDateFormat(SimpleDateFormat("yyyy/MM/dd HH:mm:ss"))

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): WrapperElement {
        val node = p.readValueAsTree<JsonNode>()

        return try {
            noLoopMapper.readValue(node.traverse())
        } catch (e: Exception) {
            val className = node["className"]
            if (className.isNull) {
                throw InvalidObjectException("${node}\n无法解析 WrapperElement: 没有类名")
            }

            handleOldElement(node, className.asText())
        }
    }

    private fun handleOldElement(node: JsonNode, className: String): WrapperElement {
        when (className) {
            PureText::class.java.name -> {
                return PureText(node["text"].asText())
            }
            AtElement::class.java.name -> {
                return AtElement(node["target"].asLong())
            }
            XmlElement::class.java.name -> {
                return XmlElement(node["content"].asText())
            }
            Picture::class.java.name -> {
                val url = node["url"]
                val filePath = node["filePath"]
                return when {
                    url.isUsable() -> {
                        Picture(url = url.asText())
                    }
                    filePath.isUsable() -> {
                        Picture(filePath = filePath.asText())
                    }
                    else -> {
                        throw InvalidObjectException("无法解析 Picture: url 或 filePath 不可用: {url=${url}, filePath=${filePath}}")
                    }
                }
            }
            else -> {
                throw InvalidObjectException("无法解析 MessageElement: 不受支持的类名 $className")
            }
        }
    }
}
