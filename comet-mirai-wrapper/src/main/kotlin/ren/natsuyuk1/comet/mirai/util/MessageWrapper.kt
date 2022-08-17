package ren.natsuyuk1.comet.mirai.util

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.utils.file.messageWrapperDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeToFile
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.message.*
import ren.natsuyuk1.comet.utils.message.Image
import java.io.File
import java.io.FileNotFoundException
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
                    cometClient.client.get(url).body<InputStream>().use {
                        it.uploadAsImage(subject)
                    }
                } else if (filePath.isNotEmpty()) {
                    if (filePath.isNotEmpty() && File(filePath).exists()) {
                        return runBlocking { File(filePath).uploadAsImage(subject) }
                    } else {
                        throw FileNotFoundException(filePath)
                    }
                } else if (base64.isNotEmpty()) {
                    return base64.toByteArray().toExternalResource().uploadAsImage(subject)
                } else {
                    throw IllegalArgumentException("Image have no argument to access image")
                }
            } catch (e: Exception) {
                logger.warn { "A error occurred when converting Image, raw content: ${toString()}" }
                return PlainText("[图片]")
            }

            return PlainText("[图片]")
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
        getMessageContent().forEach { elem ->
            kotlin.runCatching {
                runBlocking {
                    add(elem.toMessageContent(subject))
                }
            }.onFailure {
                if (it !is UnsupportedOperationException)
                    logger.warn(it) { "A error occurred when converting message wrapper" }
                else
                    logger.debug { "Unsupported message element: ${elem::class.simpleName}" }
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
                        val location = File(messageWrapperDirectory, message.imageId)
                        location.touch()

                        cometClient.client.downloadFile(
                            message.queryUrl(),
                            location
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
            is OnlineAudio -> {
                runBlocking {
                    val fileName = message.filename
                    val downloadedAudio =
                        cometClient.client.get(message.urlForDownload).body<InputStream>()

                    downloadedAudio.use {
                        val location = File(messageWrapperDirectory, fileName)
                        location.touch()

                        writeToFile(it, location)
                    }
                }
            }
            else -> {
                continue
            }
        }
    }

    return wrapper
}
