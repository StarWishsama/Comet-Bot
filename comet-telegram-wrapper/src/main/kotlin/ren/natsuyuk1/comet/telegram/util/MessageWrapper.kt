package ren.natsuyuk1.comet.telegram.util

import dev.inmo.tgbotapi.bot.exceptions.CommonRequestException
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.media.sendAudio
import dev.inmo.tgbotapi.extensions.api.send.media.sendVisualMediaGroup
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.requests.abstracts.InputFile
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.media.TelegramMediaPhoto
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.textsources.TextSource
import dev.inmo.tgbotapi.types.message.textsources.mention
import dev.inmo.tgbotapi.types.message.textsources.regular
import io.ktor.utils.io.streams.*
import ren.natsuyuk1.comet.api.message.*
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.touch
import java.io.File
import java.util.*

private val logger = mu.KotlinLogging.logger {}

suspend fun TelegramComet.send(
    message: MessageWrapper,
    type: MessageSource.MessageSourceType,
    target: ChatId,
): MessageReceipt {
    val textSourceList = mutableListOf<TextSource>()

    // 纯文本
    message.getMessageContent().forEach {
        when (it) {
            is Text -> {
                textSourceList.add(regular(it.parseToString()))
            }

            is AtElement -> {
                textSourceList.add(mention(it.userName))
            }

            else -> {}
        }
    }

    val images = message.filterIsInstance<Image>()
    val voice = message.find<Voice>()

    val resp = if (images.isNotEmpty()) {
        val photos = mutableListOf<TelegramMediaPhoto>()
        images.forEachIndexed { i, img ->
            val ifile = img.toInputFile()

            if (ifile == null) {
                logger.debug { "Unable to convert $img to telegram InputFile" }
            } else {
                val mediaPhoto = if (i == 0) TelegramMediaPhoto(
                    ifile,
                    entities = textSourceList
                ) else TelegramMediaPhoto(ifile)

                photos.add(mediaPhoto)
            }
        }

        bot.sendVisualMediaGroup(
            target,
            photos
        )
    } else if (voice != null) {
        (voice.toInputFile() ?: error("无法发送该语音 $voice")).let {
            bot.sendAudio(
                target,
                it,
                entities = textSourceList
            )
        }
    } else {
        bot.sendMessage(target, textSourceList)
    }

    return MessageReceipt(
        this,
        MessageSource(
            type,
            id,
            resp.chat.id.chatId,
            resp.date.unixMillisLong,
            resp.messageId
        )
    )
}

suspend fun MessageContent.toMessageWrapper(
    type: MessageSource.MessageSourceType,
    from: Long,
    to: Long,
    time: Long,
    msgID: Long,
    comet: TelegramComet,
    containBotAt: Boolean
): MessageWrapper {
    val receipt = MessageReceipt(comet, MessageSource(type, from, to, time, msgID))

    return when (val content = this) {
        is PhotoContent -> {
            buildMessageWrapper(receipt) {
                val photoIDs = mutableSetOf<FileId>()

                content.mediaCollection.forEach { photoIDs.add(it.fileId) }

                photoIDs.forEach {
                    val tempFile = File(cacheDirectory, it.fileId)
                    tempFile.touch()
                    tempFile.deleteOnExit()

                    kotlin.runCatching {
                        comet.bot.downloadFile(it)
                    }.onSuccess { stream ->
                        try {
                            tempFile.writeBytes(stream)
                            appendElement(Image(filePath = tempFile.absPath))
                            content.text?.let { t -> appendText(t) }
                        } catch (e: Exception) {
                            logger.warn(e) { "在转换 Telegram 图片为 Message Wrapper 时出现问题" }
                        }
                    }.onFailure { t ->
                        logger.warn(t) { "在转换 Telegram 图片为 Message Wrapper 时出现问题" }
                        return@forEach
                    }
                }
            }
        }

        is TextContent -> {
            buildMessageWrapper(receipt) {
                if (containBotAt) {
                    appendText(content.text.replace(comet.bot.getMe().username.username, ""))
                } else {
                    appendText(content.text)
                }
            }
        }

        else -> MessageWrapper().setUsable(false)
    }
}

fun Image.toInputFile(): InputFile? {
    return try {
        when {
            url?.isNotBlank() == true -> InputFile.fromUrl(url!!)
            filePath?.isNotBlank() == true -> InputFile.fromFile(File(filePath!!))
            !base64.isNullOrBlank() ->
                Base64
                    .getMimeDecoder()
                    .decode(base64!!)
                    .asMultipartFile(System.currentTimeMillis().toString() + ".png")

            stream != null -> stream!!.asInput().use {
                InputFile.fromInput(System.currentTimeMillis().toString() + ".png") { it }
            }

            else -> null
        }
    } catch (e: CommonRequestException) {
        logger.warn(e) { "Unable to convert $this to telegram Inputfile" }
        null
    }
}

fun Voice.toInputFile(): InputFile? = if (filePath.isNotBlank()) InputFile.fromFile(File(filePath)) else null
