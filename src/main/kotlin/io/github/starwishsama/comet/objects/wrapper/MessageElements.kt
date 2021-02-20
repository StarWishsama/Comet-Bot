package io.github.starwishsama.comet.objects.wrapper

import com.google.gson.*
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File
import java.lang.reflect.Type

interface WrapperElement {
    val className: String

    fun toMessageContent(subject: Contact): MessageContent

    fun asString(): String
}

data class PureText(val text: String): WrapperElement {
    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact): PlainText {
        return PlainText(text)
    }

    override fun asString(): String {
        return text
    }
}

@Serializable
data class Picture(val url: String, val filePath: String = ""): WrapperElement {
    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact): Image {
        if (url.isNotEmpty()) {
            NetUtil.getInputStream(url)?.use {
                return runBlocking { it.uploadAsImage(subject) }
            }
        } else if (filePath.isNotEmpty() && File(filePath).exists()) {
            return runBlocking { File(filePath).uploadAsImage(subject) }
        }


        throw IllegalArgumentException("url or filePath can't be null or empty!")
    }

    override fun asString(): String {
        return if (filePath.isEmpty()) url else filePath
    }
}

@Serializable
data class AtElement(val target: Long): WrapperElement {
    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact): MessageContent {
        return At(target)
    }

    override fun asString(): String = "@${target}"

}

@Serializable
data class XmlElement(val content: String): WrapperElement {
    override val className: String = this::class.java.name

    @OptIn(MiraiExperimentalApi::class)
    override fun toMessageContent(subject: Contact): MessageContent {
        return SimpleServiceMessage(serviceId = 60, content = content)
    }

    override fun asString(): String = "XML 消息"

}

class WrapperElementAdapter : JsonDeserializer<Any>, JsonSerializer<Any> {
    @Throws(JsonParseException::class)
    override fun deserialize(jsonElement: JsonElement, type: Type,
                             jsonDeserializationContext: JsonDeserializationContext
    ): Any {
        val jsonObject = jsonElement.asJsonObject
        val className = jsonObject.get("className").asString
        val objectClass = getObjectClass(className)
        return jsonDeserializationContext.deserialize(jsonObject, objectClass)
    }

    override fun serialize(jsonElement: Any, type: Type, jsonSerializationContext: JsonSerializationContext): JsonElement {
        val element = jsonSerializationContext.serialize(jsonElement).asJsonObject
        element.addProperty("className", jsonElement.javaClass.name)
        return element
    }


    private fun getObjectClass(className: String): Class<*> {
        try {
            return Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw JsonParseException(e.message)
        }
    }
}