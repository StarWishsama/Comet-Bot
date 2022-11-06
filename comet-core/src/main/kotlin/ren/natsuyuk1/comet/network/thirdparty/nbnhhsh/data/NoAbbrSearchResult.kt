package ren.natsuyuk1.comet.network.thirdparty.nbnhhsh.data

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.message.buildMessageWrapper

@Serializable
data class NoAbbrSearchResult(
    val name: String,
    val trans: List<String>? = null,
    val inputting: List<String>? = null,
) {
    fun toMessageWrapper() = buildMessageWrapper {
        appendTextln("🔍 能不能好好说话？")
        if (trans.isNullOrEmpty()) {
            appendText("找不到 $name 的缩写释义捏")
        } else {
            appendText("$name: ${trans.format()}")
        }
    }
}

private fun List<String>.format(): String =
    buildString {
        this@format.forEach {
            append("$it, ")
        }
    }.removeSuffix(", ")
