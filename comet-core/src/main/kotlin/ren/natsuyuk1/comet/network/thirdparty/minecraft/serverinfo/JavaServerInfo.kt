package ren.natsuyuk1.comet.network.thirdparty.minecraft.serverinfo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class JavaServerInfo(
    val version: VersionInfo,
    val players: PlayerInfo,
    @SerialName("description")
    val motd: JsonObject,
    @SerialName("modinfo")
    val modInfo: ModInfo? = null,
    val favicon: String? = null,
) {
    @Serializable
    data class VersionInfo(
        @SerialName("name")
        val protocolName: String,
        @SerialName("protocol")
        val protocolVersion: Int
    )

    @Serializable
    data class PlayerInfo(
        @SerialName("max")
        val maxPlayer: Int,
        @SerialName("online")
        val onlinePlayer: Int
    )

    @Serializable
    data class ModInfo(
        val type: String,
        @SerialName("modList")
        val modList: List<Mod>
    ) {
        @Serializable
        data class Mod(
            @SerialName("modid")
            val modID: String,
            @SerialName("version")
            val version: String
        )
    }
}
