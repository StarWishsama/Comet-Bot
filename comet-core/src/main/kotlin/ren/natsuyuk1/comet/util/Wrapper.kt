package ren.natsuyuk1.comet.util

import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper

fun String.toMessageWrapper(appendPrefix: String = CometGlobalConfig.data.prefix): MessageWrapper =
    buildMessageWrapper {
        appendText(appendPrefix)
        appendText(this@toMessageWrapper)
    }
