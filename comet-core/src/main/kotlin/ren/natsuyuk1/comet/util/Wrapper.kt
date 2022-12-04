package ren.natsuyuk1.comet.util

import ren.natsuyuk1.comet.api.command.CommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper

fun String.toMessageWrapper(appendPrefix: String = CometConfig.data.prefix): MessageWrapper =
    buildMessageWrapper {
        appendText(appendPrefix)
        appendText(this@toMessageWrapper)
    }

suspend inline fun CommandSender.sendMessage(message: String): MessageReceipt? =
    sendMessage(message.toMessageWrapper())
