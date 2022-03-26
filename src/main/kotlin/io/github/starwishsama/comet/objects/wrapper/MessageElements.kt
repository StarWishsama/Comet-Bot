/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.wrapper

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.utils.StringUtil.base64ToImage
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.uploadAsImage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SimpleServiceMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "className")
@JsonSubTypes(
    JsonSubTypes.Type(value = PureText::class, name = "io.github.starwishsama.comet.objects.wrapper.PureText"),
    JsonSubTypes.Type(value = Picture::class, name = "io.github.starwishsama.comet.objects.wrapper.Picture"),
    JsonSubTypes.Type(value = AtElement::class, name = "io.github.starwishsama.comet.objects.wrapper.AtElement"),
    JsonSubTypes.Type(value = XmlElement::class, name = "io.github.starwishsama.comet.objects.wrapper.XmlElement")
)
interface WrapperElement {
    val className: String

    fun toMessageContent(subject: Contact?): MessageContent

    fun asString(): String
}

/**
 * [PureText]
 *
 * 纯文本消息
 *
 * @param text 文本
 */
data class PureText(val text: String) : WrapperElement {
    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact?): PlainText {
        return PlainText(text)
    }

    override fun asString(): String = text
}

/**
 * [Picture]
 *
 * 图片消息
 *
 * 必须提供图片下载链接, 图片本地路径及 Base64 之中其一.
 *
 * @param url 图片下载链接
 * @param filePath 图片本地路径
 */
@Serializable
data class Picture(
    val url: String = "",
    val filePath: String = "",
    val base64: String = "",
    val fileFormat: String = ""
) : WrapperElement {

    init {
        if (url.isEmpty() && filePath.isEmpty() && base64.isEmpty()) {
            throw IllegalArgumentException("url/filePath/base64 can't be null or empty!")
        }
    }

    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact?): MessageContent {
        if (subject == null) {
            return PlainText("[图片]")
        }

        try {
            if (url.isNotEmpty()) {
                NetUtil.getInputStream(url)?.use {
                    return runBlocking {
                        it.uploadAsImage(subject, fileFormat.ifEmpty { null })
                    }
                }
            } else if (filePath.isNotEmpty()) {
                if (filePath.isNotEmpty() && File(filePath).exists()) {
                    return runBlocking { File(filePath).uploadAsImage(subject, fileFormat.ifEmpty { null }) }
                }
            } else if (base64.isNotEmpty()) {
                return base64.toByteArray().base64ToImage().uploadAsImage(subject)
            }
        } catch (e: Exception) {
            CometVariables.daemonLogger.warning("在转换图片时出现了问题, Wrapper 原始内容为: ${toString()}")
            return PlainText("[图片]")
        }

        throw RuntimeException("Unable to convert Picture to Image, Picture raw content: $this")
    }

    override fun asString(): String = "[图片]"
}

/**
 * [AtElement]
 *
 * At 消息
 *
 * @param target At 目标
 */
@Serializable
data class AtElement(val target: Long) : WrapperElement {
    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact?): MessageContent {
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
data class XmlElement(val content: String) : WrapperElement {
    override val className: String = this::class.java.name

    @OptIn(MiraiExperimentalApi::class)
    override fun toMessageContent(subject: Contact?): MessageContent {
        return SimpleServiceMessage(serviceId = 60, content = content)
    }

    override fun asString(): String = "[XML 消息]"

}

data class Voice(val filePath: String) : WrapperElement {
    override val className: String = this::class.java.name

    override fun toMessageContent(subject: Contact?): MessageContent {
        requireNotNull(subject) { "subject cannot be null!" }

        if (subject !is AudioSupported) {
            return PlainText("语音消息只能发送给好友或群")
        }

        if (filePath.isNotEmpty() && File(filePath).exists()) {
            return runBlocking {
                subject.uploadAudio(File(filePath).toExternalResource())
            }
        }

        throw RuntimeException("Unable to convert Voice to MessageChain, Raw path: $this")
    }

    override fun asString(): String = "[语音消息]"
}