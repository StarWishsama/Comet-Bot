package ren.natsuyuk1.comet.telegram.util

import dev.inmo.tgbotapi.abstracts.TextedWithTextSources
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.media.sendAudio
import dev.inmo.tgbotapi.extensions.api.send.media.sendPhoto
import dev.inmo.tgbotapi.extensions.api.send.media.sendVisualMediaGroup
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.requests.abstracts.InputFile
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.media.TelegramMediaPhoto
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.VoiceContent
import dev.inmo.tgbotapi.types.message.textsources.*
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.message.*
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.utils.datetime.getLastingTimeAsString
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.touch
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

private val logger = mu.KotlinLogging.logger {}

suspend fun TelegramComet.send(
    message: MessageWrapper,
    type: MessageSource.MessageSourceType,
    target: ChatId,
    replyMessageId: MessageId? = null,
): MessageReceipt {
    val executeTime = Clock.System.now()

    val textSource = mutableListOf<TextSource>()

    // 纯文本
    message.getMessageContent().forEach {
        when (it) {
            is Text -> {
                textSource.add(regular(it.parseToString()))
            }

            is AtElement -> {
                textSource.add(mention(it.userName))
            }

            else -> {}
        }
    }

    val images = message.filterIsInstance<Image>()
    val voice = message.find<Voice>()

    val resp = if (images.isNotEmpty()) {
        if (images.size == 1) {
            val ifile = images.first().toInputFile() ?: error("Unable to convert ${images.first()} to inputfile")

            bot.sendPhoto(
                target,
                ifile,
                entities = textSource,
                replyToMessageId = replyMessageId,
            )
        } else {
            val photos = mutableListOf<TelegramMediaPhoto>()

            images.forEachIndexed { i, img ->
                val ifile = img.toInputFile()

                if (ifile == null) {
                    logger.debug { "Unable to convert $img to telegram InputFile" }
                } else {
                    val mediaPhoto = if (i == 0) {
                        TelegramMediaPhoto(
                            ifile,
                            entities = textSource,
                        )
                    } else {
                        TelegramMediaPhoto(ifile)
                    }

                    photos.add(mediaPhoto)
                }
            }

            bot.sendVisualMediaGroup(
                target,
                photos,
                replyToMessageId = replyMessageId,
            )
        }
    } else if (voice != null) {
        (voice.toInputFile() ?: error("无法发送该语音 $voice")).let {
            bot.sendAudio(
                target,
                it,
                entities = textSource,
                replyToMessageId = replyMessageId,
            )
        }
    } else {
        bot.sendMessage(target, textSource, replyToMessageId = replyMessageId)
    }

    return MessageReceipt(
        this,
        MessageSource(
            type,
            id,
            resp.chat.id.chatId,
            resp.date.unixMillisLong,
            resp.messageId,
        ),
    ).also {
        logger.debug { "Send message costs ${executeTime.getLastingTimeAsString(TimeUnit.MILLISECONDS)}" }
    }
}

suspend fun MessageContent.toMessageWrapper(
    type: MessageSource.MessageSourceType,
    from: Long,
    to: Long,
    time: Long,
    msgID: Long,
    comet: TelegramComet,
): MessageWrapper {
    val receipt = MessageReceipt(comet, MessageSource(type, from, to, time, msgID))
    val textSource = (this as? TextedWithTextSources)?.textSources?.map {
        when (it) {
            is MentionTextSource -> {
                try {
                    AtElement(comet.bot.getChat(it.username).id.chatId)
                } catch (e: Exception) {
                    AtElement(userName = it.source)
                }
            }

            is BotCommandTextSource -> Text(it.asText.replace(comet.bot.getMe().username.username, ""))
            else -> Text(it.asText)
        }
    }

    return when (this) {
        is PhotoContent -> {
            buildMessageWrapper(receipt) {
                val photoIDs = mutableSetOf<FileId>()

                mediaCollection.forEach { photoIDs.add(it.fileId) }

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
                            textSource?.forEach { t -> appendElement(t) }
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

        is VoiceContent -> {
            buildMessageWrapper(receipt) {
                val dest = File(cacheDirectory, media.fileId.fileId)
                dest.touch()
                dest.deleteOnExit()
                comet.bot.downloadFile(media, dest)
                appendElement(Voice(dest.absPath))

                textSource?.forEach { t -> appendElement(t) }
            }
        }

        is TextContent -> {
            buildMessageWrapper(receipt) {
                textSource?.forEach { t -> appendElement(t) }
            }
        }

        else -> MessageWrapper().setUsable(false)
    }
}

fun Image.toInputFile(): InputFile? {
    return try {
        when {
            !url.isNullOrBlank() -> url?.let { InputFile.fromUrl(it) }
            !filePath.isNullOrBlank() -> filePath?.let { InputFile.fromFile(File(it)) }
            !base64.isNullOrBlank() ->
                base64?.let {
                    Base64.getMimeDecoder()
                        .decode(it)
                        .asMultipartFile(System.currentTimeMillis().toString() + ".png")
                }

            byteArray != null -> byteArray?.let { it.asMultipartFile(System.currentTimeMillis().toString() + ".png") }

            else -> null
        }
    } catch (e: Exception) {
        logger.warn(e) { "Unable to convert $this to telegram Inputfile" }
        null
    }
}

fun Voice.toInputFile(): InputFile? = if (filePath.isNotBlank()) InputFile.fromFile(File(filePath)) else null
