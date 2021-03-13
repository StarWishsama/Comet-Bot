package io.github.starwishsama.comet.objects.wrapper

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.starwishsama.comet.utils.json.WrapperConverter
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = WrapperConverter::class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(value = PureText::class, name = "PureText"),
    JsonSubTypes.Type(value = Picture::class, name = "Picture"),
    JsonSubTypes.Type(value = AtElement::class, name = "AtElement"),
    JsonSubTypes.Type(value = XmlElement::class, name = "XmlElement")
)
interface WrapperElement {
    val className: String

    fun toMessageContent(subject: Contact): MessageContent

    fun asString(): String
}

/**
 * [PureText]
 *
 * 纯文本消息
 *
 * @param text 文本
 */
data class PureText(val text: String): WrapperElement {
    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact): PlainText {
        return PlainText(text)
    }

    override fun asString(): String {
        return text
    }
}

/**
 * [Picture]
 *
 * 图片消息
 *
 * 必须提供图片下载链接和图片本地路径之中其一.
 *
 * @param url 图片下载链接
 * @param filePath 图片本地路径
 */
@Serializable
data class Picture(val url: String = "", val filePath: String = ""): WrapperElement {

    init {
        if (url.isEmpty() && filePath.isEmpty()) {
            throw IllegalArgumentException("url or filePath can't be null or empty!")
        }
    }

    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact): Image {
        if (url.isNotEmpty()) {
            NetUtil.getInputStream(url)?.use {
                return runBlocking { it.uploadAsImage(subject) }
            }
        } else {
            if (filePath.isNotEmpty() && File(filePath).exists()) {
                return runBlocking { File(filePath).uploadAsImage(subject) }
            }
        }

        throw RuntimeException("Unable to convert Picture to Image, Picture raw content: $this")
    }

    override fun asString(): String {
        return if (filePath.isEmpty()) url else filePath
    }
}

/**
 * [AtElement]
 *
 * At 消息
 *
 * @param target At 目标
 */
@Serializable
data class AtElement(val target: Long): WrapperElement {
    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact): MessageContent {
        return At(target)
    }

    override fun asString(): String = "@${target}"

}

/**
 * [XmlElement]
 *
 * XML 消息
 *
 * @param content XML 消息
 */
@Serializable
data class XmlElement(val content: String): WrapperElement {
    override val className: String = this::class.java.name

    @OptIn(MiraiExperimentalApi::class)
    override fun toMessageContent(subject: Contact): MessageContent {
        return SimpleServiceMessage(serviceId = 60, content = content)
    }

    override fun asString(): String = "XML 消息"

}