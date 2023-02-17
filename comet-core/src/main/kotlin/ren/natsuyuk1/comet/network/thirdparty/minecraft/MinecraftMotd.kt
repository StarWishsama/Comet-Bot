package ren.natsuyuk1.comet.network.thirdparty.minecraft

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.minecraft.serverinfo.BedrockServerInfo
import ren.natsuyuk1.comet.network.thirdparty.minecraft.serverinfo.JavaServerInfo

val colorCodeRegex = Regex("§[A-Za-z0-9]")

@Serializable
data class QueryInfo(
    val payload: String,
    val type: MinecraftServerType,
    val ping: Long,
) {
    fun toMessageWrapper(): MessageWrapper =
        when (type) {
            MinecraftServerType.JAVA -> json.decodeFromString<JavaServerInfo>(payload).toMessageWrapper(ping)
            MinecraftServerType.BEDROCK -> BedrockServerInfo.convert(payload).toMessageWrapper(ping)
        }
}
