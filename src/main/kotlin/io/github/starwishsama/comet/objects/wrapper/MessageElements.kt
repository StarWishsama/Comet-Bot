package io.github.starwishsama.comet.objects.wrapper

import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File

@Serializable
open class WrapperElement {
    open fun toMessageContent(subject: Contact): MessageContent {
        throw UnsupportedOperationException("You have to override this method!")
    }

    open fun asString(): String {
        throw UnsupportedOperationException("You have to override this method!")
    }
}

@Serializable
data class PureText(val text: String): WrapperElement() {
    override fun toMessageContent(subject: Contact): PlainText {
        return PlainText(text)
    }

    override fun asString(): String {
        return text
    }
}

@Serializable
data class Picture(val url: String, @Contextual val filePath: File? = null): WrapperElement() {
    override fun toMessageContent(subject: Contact): Image {
        if (url.isNotEmpty()) {
            NetUtil.getInputStream(url)?.use {
                return runBlocking { it.uploadAsImage(subject) }
            }
        } else if (filePath != null) {
            return runBlocking { filePath.uploadAsImage(subject) }
        }

        throw IllegalArgumentException("url or filePath can't be null or empty!")
    }

    override fun asString(): String {
        return filePath?.absolutePath ?: url
    }
}

@Serializable
data class AtElement(val target: Long): WrapperElement() {
    override fun toMessageContent(subject: Contact): MessageContent {
        return At(target)
    }

    override fun asString(): String = "@${target}"

}

@Serializable
data class XmlElement(val content: String): WrapperElement() {
    @OptIn(MiraiExperimentalApi::class)
    override fun toMessageContent(subject: Contact): MessageContent {
        return SimpleServiceMessage(serviceId = 60, content = content)
    }

    override fun asString(): String = "XML 消息"

}