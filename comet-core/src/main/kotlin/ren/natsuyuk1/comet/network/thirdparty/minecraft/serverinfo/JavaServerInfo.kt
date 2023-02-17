package ren.natsuyuk1.comet.network.thirdparty.minecraft.serverinfo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.minecraft.colorCodeRegex

@Serializable
data class JavaServerInfo(
    val version: VersionInfo,
    val players: PlayerInfo,
    @SerialName("description")
    val motd: JsonElement,
    @SerialName("modinfo")
    val modInfo: ModInfo? = null,
    val favicon: String? = null,
) {
    @Serializable
    data class VersionInfo(
        @SerialName("name")
        val protocolName: String,
        @SerialName("protocol")
        val protocolVersion: Int,
    )

    @Serializable
    data class PlayerInfo(
        @SerialName("max")
        val maxPlayer: Int,
        @SerialName("online")
        val onlinePlayer: Int,
    )

    @Serializable
    data class ModInfo(
        val type: String,
        @SerialName("modList")
        val modList: List<Mod>,
    ) {
        @Serializable
        data class Mod(
            @SerialName("modid")
            val modID: String,
            @SerialName("version")
            val version: String,
        )
    }

    private fun parseMOTD(): String {
        if (motd as? JsonPrimitive != null) {
            return motd.content
        }

        return buildString {
            if (motd.jsonObject["extra"] == null) {
                append(motd.jsonObject["text"])
            } else {
                motd.jsonObject["extra"]?.jsonArray?.forEach {
                    append(it.jsonObject["text"]?.jsonPrimitive?.content)
                }
            }
        }.trim()
    }

    fun toMessageWrapper(ping: Long): MessageWrapper =
        buildMessageWrapper {
            if (favicon != null) {
                appendElement(Image(base64 = favicon.split(",")[1]))
            }
            appendText(
                """
Java 版服务器                
                
> 在线玩家 ${players.onlinePlayer}/${players.maxPlayer}
> MOTD ${parseMOTD().replace(colorCodeRegex, "")}
> 服务器版本 ${version.protocolName}
> 延迟 ${ping}ms
${if (!modInfo?.modList.isNullOrEmpty()) "> MOD 列表 " + modInfo!!.modList else ""}
                """.trimIndent(),
            )
        }
}
