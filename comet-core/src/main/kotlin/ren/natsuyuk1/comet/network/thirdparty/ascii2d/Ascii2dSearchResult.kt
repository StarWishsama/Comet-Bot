package ren.natsuyuk1.comet.network.thirdparty.ascii2d

import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper

data class Ascii2dSearchResult(
    val author: String,
    val originalURL: String,
    val errorMessage: String = ""
)

fun Ascii2dSearchResult.toMessageWrapper(): MessageWrapper =
    if (errorMessage.isBlank()) {
        buildMessageWrapper {
            appendTextln("✔ 已找到可能的图片来源")
            appendTextln("\uD83C\uDFF7 来自 $author 的作品")
            appendText("🔗 $originalURL")
        }
    } else {
        buildMessageWrapper {
            appendText(errorMessage)
        }
    }
