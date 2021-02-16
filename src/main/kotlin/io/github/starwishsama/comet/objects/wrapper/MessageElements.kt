package io.github.starwishsama.comet.objects.wrapper

import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.File

interface WrapperElement {
    fun toMessageContent(subject: Contact): MessageContent

    fun asString(): String
}

data class PureText(val text: String): WrapperElement {
    override fun toMessageContent(subject: Contact): PlainText {
        return PlainText(text)
    }

    override fun asString(): String {
        return text
    }
}

data class Picture(val url: String, val filePath: File? = null): WrapperElement {
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

data class AtElement(val target: Long): WrapperElement {
    override fun toMessageContent(subject: Contact): MessageContent {
        return At(target)
    }

    override fun asString(): String = "@${target}"

}