package ren.natsuyuk1.comet.network.thirdparty.nbnhhsh.data

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper

@Serializable
data class NoAbbrSearchResult(
    val name: String,
    val trans: List<String>
) {
    fun toMessageWrapper() = buildMessageWrapper {
        appendText("🔍 能不能好好说话？", true)
        if (trans.isEmpty()) {
            appendText("找不到 $name 的缩写释义捏")
        } else {
            appendText("$name: ${trans.take(5).format()}")
        }
    }
}

private fun List<String>.format(): String =
    buildString {
        this@format.forEach {
            append("$it, ")
        }
    }.removeSuffix(", ")
