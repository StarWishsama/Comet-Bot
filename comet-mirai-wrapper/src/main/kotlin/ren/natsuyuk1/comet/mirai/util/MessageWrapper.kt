/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.mirai.util

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import ren.natsuyuk1.comet.consts.client
import ren.natsuyuk1.comet.utils.file.messageWrapperDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.message.*
import ren.natsuyuk1.comet.utils.message.Image
import java.io.File
import java.io.InputStream

private val logger = mu.KotlinLogging.logger("MessageWrapperConverter")

@OptIn(MiraiExperimentalApi::class)
suspend fun WrapperElement.toMessageContent(subject: Contact?): MessageContent {
    when (this) {
        is Text -> return PlainText(this.text)

        is Image -> {
            if (subject == null) {
                return PlainText("[图片]")
            }

            try {
                if (url.isNotEmpty()) {
                    client.client.get<HttpStatement>(url).receive<InputStream>().use {
                        it.uploadAsImage(subject)
                    }
                } else if (filePath.isNotEmpty()) {
                    if (filePath.isNotEmpty() && File(filePath).exists()) {
                        return runBlocking { File(filePath).uploadAsImage(subject) }
                    }
                } else if (base64.isNotEmpty()) {
                    return base64.toByteArray().toExternalResource().uploadAsImage(subject)
                }
            } catch (e: Exception) {
                logger.warn { "A error occurred when converting Image, raw content: ${toString()}" }
                return PlainText("[图片]")
            }

            throw RuntimeException("Unable to convert image, Image raw content: $this")
        }

        is AtElement -> return At(target)

        is XmlElement -> return SimpleServiceMessage(serviceId = 60, content = content)

        is Voice -> {
            requireNotNull(subject) { "subject cannot be null!" }

            if (subject !is AudioSupported) {
                throw UnsupportedOperationException("Sending voice message to unsupported subject: must be friend or group")
            }

            if (filePath.isNotEmpty() && File(filePath).exists()) {
                return runBlocking {
                    subject.uploadAudio(File(filePath).toExternalResource())
                }
            }

            throw RuntimeException("Unable to convert Voice to MessageChain, Raw content: $this")
        }

        else -> throw UnsupportedOperationException("Unsupported message wrapper ${this::class.simpleName} in mirai side")
    }
}

/**
 * [toMessageChain]
 *
 * 将一个 [MessageWrapper] 转换为 [MessageChain]
 *
 * @param subject Mirai 的 [Contact], 为空时一些需要 [Contact] 的元素会转为文字
 */
fun MessageWrapper.toMessageChain(subject: Contact? = null): MessageChain {
    return MessageChainBuilder().apply {
        getMessageContent().forEach {
            kotlin.runCatching {
                runBlocking {
                    add(it.toMessageContent(subject))
                }
            }.onFailure {
                logger.warn(it) { "A error occurred when converting message wrapper" }
            }
        }
    }.build()
}

fun MessageChain.toMessageWrapper(localImage: Boolean = false): MessageWrapper {
    val wrapper = MessageWrapper()
    for (message in this) {
        when (message) {
            is PlainText -> {
                wrapper.appendText(message.content)
            }
            is net.mamoe.mirai.message.data.Image -> {
                runBlocking {
                    if (localImage) {
                        messageWrapperDirectory.touch()
                        val location = client.client.downloadFile(
                            message.queryUrl(),
                            File(messageWrapperDirectory, message.imageId)
                        )
                        wrapper.appendElement(Image(filePath = location.canonicalPath))
                    } else {
                        wrapper.appendElement(Image(url = message.queryUrl()))
                    }
                }
            }
            is At -> {
                wrapper.appendElement(AtElement(message.target))
            }
            is ServiceMessage -> {
                wrapper.appendElement(XmlElement(message.content))
            }
            is Audio -> {
                TODO("Not implemented")
            }
            else -> {
                continue
            }
        }
    }

    return wrapper
}
