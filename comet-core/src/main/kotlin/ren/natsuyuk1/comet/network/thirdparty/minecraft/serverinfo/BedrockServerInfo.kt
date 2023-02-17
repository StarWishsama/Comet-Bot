package ren.natsuyuk1.comet.network.thirdparty.minecraft.serverinfo

import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.minecraft.colorCodeRegex

data class BedrockServerInfo(
    val version: String,
    val online: String,
    val maxOnline: String,
    val gameMode: String,
    val agreement: String,
    val motd: String,
) {
    fun toMessageWrapper(usedTime: Long): MessageWrapper =
        buildMessageWrapper {
            appendText(
                """
            基岩版服务器                
               
            > 在线玩家 $online/$maxOnline
            > MOTD ${motd.replace(colorCodeRegex, "")}
            > 服务器版本 $version
            > 延迟 ${usedTime}ms    
                """.trimIndent(),
            )
        }

    companion object {
        fun convert(raw: String): BedrockServerInfo {
            val split = raw.split(";")
            val motd: String = split[1].replace(colorCodeRegex, "")
            val agreement = split[2]
            val version = split[3]
            val online = split[4]
            val max = split[5]
            val gameMode = split[8]

            return BedrockServerInfo(version, online, max, gameMode, agreement, motd)
        }
    }
}
