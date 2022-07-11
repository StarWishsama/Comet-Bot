package ren.natsuyuk1.comet.telegram.util

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.utils.message.Image
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.Text
import ren.natsuyuk1.comet.utils.message.Voice
import java.io.File

fun MessageWrapper.send(comet: TelegramComet, target: ChatId) =
    getMessageContent().forEach { elem ->
        when (elem) {
            is Text -> comet.bot.sendMessage(target, elem.asString())
            is Image -> elem.toTelegramFile()?.let { comet.bot.sendPhoto(target, it) }
            is Voice -> elem.toTelegramFile()?.let { comet.bot.sendAudio(target, it) }
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
