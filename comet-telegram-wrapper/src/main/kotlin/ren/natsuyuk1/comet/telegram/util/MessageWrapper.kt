package ren.natsuyuk1.comet.telegram.util

import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.get.getFileAdditionalInfo
import dev.inmo.tgbotapi.extensions.api.send.media.sendAudio
import dev.inmo.tgbotapi.extensions.api.send.media.sendPhoto
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.requests.abstracts.InputFile
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.textsources.TextSource
import dev.inmo.tgbotapi.types.message.textsources.mention
import dev.inmo.tgbotapi.types.message.textsources.regular
import ren.natsuyuk1.comet.api.message.*
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.touch
import java.io.File

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

    val image = message.find<Image>()
    val voice = message.find<Voice>()

    val resp = if (image != null) {
        (image.toInputFile() ?: error("无法发送该图片 $image")).let {
            bot.sendPhoto(
                target,
                it,
                entities = textSourceList
            )
        }
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
                        Pair(comet.bot.downloadFile(it), comet.bot.getFileAdditionalInfo(it))
                    }.onSuccess { (localPath, urlPath) ->
                        try {
                            tempFile.writeBytes(localPath)
                            // Provide a temp url for pic search (expire after 1h)
                            val url = "https://api.telegram.org/file/bot${comet.config.password}/${urlPath.filePath}"
                            appendElement(Image(filePath = tempFile.absPath, url = url))
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
    return when {
        url?.isNotBlank() == true -> InputFile.fromUrl(url!!)
        filePath?.isNotBlank() == true -> InputFile.fromFile(File(filePath!!))
        else -> null
    }
}

fun Voice.toInputFile(): InputFile? = if (filePath.isNotBlank()) InputFile.fromFile(File(filePath)) else null
