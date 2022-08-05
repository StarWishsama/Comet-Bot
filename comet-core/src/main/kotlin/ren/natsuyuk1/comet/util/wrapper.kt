package ren.natsuyuk1.comet.util

import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper

fun String.toMessageWrapper(appendPrefix: String = CometGlobalConfig.data.prefix): MessageWrapper =
    buildMessageWrapper {
        appendText(appendPrefix)
        appendText(this@toMessageWrapper)
    }
