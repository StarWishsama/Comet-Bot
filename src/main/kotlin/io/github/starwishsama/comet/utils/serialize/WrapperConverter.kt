/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils.serialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.starwishsama.comet.objects.wrapper.*
import java.io.InvalidObjectException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object WrapperConverter : JsonDeserializer<WrapperElement>() {
    private val noLoopMapper: ObjectMapper = ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        .registerKotlinModule()
        .registerModules(
            JavaTimeModule().also {
                it.addSerializer(
                    LocalDateTime::class.java,
                    LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
                )
                it.addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                it.addSerializer(LocalTime::class.java, LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                it.addDeserializer(
                    LocalDate::class.java,
                    LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                )
                it.addDeserializer(
                    LocalTime::class.java,
                    LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss"))
                )
            },
            KotlinModule.Builder().enable(KotlinFeature.NullIsSameAsDefault)
                .enable(KotlinFeature.NullToEmptyCollection)
                .enable(KotlinFeature.NullToEmptyMap).build(),
            SimpleModule().also {
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
