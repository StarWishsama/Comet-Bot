package ren.natsuyuk1.comet.telegram.util

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.message.*
import java.io.File

private val logger = mu.KotlinLogging.logger {}

fun MessageWrapper.send(comet: TelegramComet, target: ChatId) {
    val textBuffer = StringBuffer()

    getMessageContent().asSequence().filterIsInstance<Text>().forEach { textBuffer.append(it.asString()) }

    if (find<Image>() != null) {
        find<Image>()?.toTelegramFile()?.let { comet.bot.sendPhoto(target, it, caption = textBuffer.toString()) }
    } else if (find<Voice>() != null) {
        find<Voice>()?.toTelegramFile()?.let { comet.bot.sendAudio(target, it) }
    }

    comet.bot.sendMessage(target, textBuffer.toString())
}

suspend fun Message.toMessageWrapper(comet: TelegramComet, permanent: Boolean = false): MessageWrapper {
    return when {
        photo != null -> {
            buildMessageWrapper {
                val photoIDs = mutableSetOf<String>()

                photo?.forEach { photoIDs.add(it.fileId) }

                photoIDs.forEach {
                    val tempFile = File(cacheDirectory, it)
                    tempFile.touch()
                    if (!permanent) tempFile.deleteOnExit()

                    val (file, e) = comet.bot.getFile(it)

                    if (e != null || file == null || !file.isSuccessful) {
                        logger.warn(e) { "在转换 Telegram 图片为 MessageWrapper 时出现问题" }
                        return@forEach
                    }

                    kotlin.runCatching {
                        cometClient.client.downloadFile(
                            "https://api.telegram.org/file/bot${comet.telegramConfig.token}/${file.body()?.result?.fileId}",
                            tempFile
                        )
                    }.onFailure { t ->
                        logger.warn(t) { "在转换 Telegram 图片为 MessageWrapper 时出现问题" }
                    }.onSuccess {
                        appendElement(Image(filePath = tempFile.canonicalPath))
                        this@toMessageWrapper.caption?.let { it1 -> appendText(it1) }
                    }
                }
            }
        }
        else -> buildMessageWrapper {
            this@toMessageWrapper.text?.let { appendText(it) }
        }
    }
}

fun Image.toTelegramFile(): TelegramFile? {
    return when {
        url.isNotBlank() -> TelegramFile.ByUrl(url)
        filePath.isNotBlank() -> TelegramFile.ByFile(File(filePath))
        else -> null
    }
}

fun Voice.toTelegramFile(): TelegramFile? = if (filePath.isNotBlank()) TelegramFile.ByFile(File(filePath)) else null
