/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects.minecraft

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.Picture

private val colorCodeRegex = Regex("§[A-Za-z0-9]")

interface MinecraftServerInfo {
    fun getStatus(usedTime: Long): MessageWrapper
}

/**
 * [MinecraftJavaInfo]
 *
 * Minecraft Java 版本的服务器 MOTD 信息
 */
data class MinecraftJavaInfo(
    val version: VersionInfo,
    val players: PlayerInfo,
    @JsonProperty("description")
    val motd: JsonNode,
    @JsonProperty("modinfo")
    val modInfo: ModInfo?,
    @JsonProperty("favicon")
    val favicon: String?
) : MinecraftServerInfo {
    override fun getStatus(usedTime: Long): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addText(
            """
Java 版服务器                
                
> 在线玩家 ${players.onlinePlayer}/${players.maxPlayer}
> MOTD ${parseMOTD().replace(colorCodeRegex, "")}
> 服务器版本 ${version.protocolName}
> 延迟 ${usedTime}ms
${if (modInfo?.modList != null) "> MOD 列表 " + modInfo.modList else ""}
        """.trimIndent()
        )

        if (favicon != null) {
            wrapper.addElement(Picture(base64 = favicon.split(",")[1]))
        }

        return wrapper
    }

    private fun parseMOTD(): String {
        if (motd.isTextual) {
            return motd.asText()
        }

        return buildString {
            if (motd["extra"] == null) {
                append(motd["text"])
            } else {
                motd["extra"].forEach {
                    append(it["text"].asText())
                }
            }
        }.trim()
    }

    data class VersionInfo(
        @JsonProperty("name")
        val protocolName: String,
        @JsonProperty("protocol")
        val protocolVersion: Int
    )

    data class PlayerInfo(
        @JsonProperty("max")
        val maxPlayer: Int,
        @JsonProperty("online")
        val onlinePlayer: Int
    )

    data class ModInfo(
        val type: String,
        @JsonProperty("modList")
        val modList: List<Mod>
    ) {
        data class Mod(
            @JsonProperty("modid")
            val modID: String,
            @JsonProperty("version")
            val version: String
        )
    }
}


data class MinecraftBedrockInfo(
    val version: String,
    val online: String,
    val maxOnline: String,
    val gameMode: String,
    val agreement: String,
    val motd: String
) : MinecraftServerInfo {
    override fun getStatus(usedTime: Long): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addText(
            """
基岩版服务器                
               
> 在线玩家 ${online}/${maxOnline}
> MOTD ${motd.replace(colorCodeRegex, "")}
> 服务器版本 $version
> 延迟 ${usedTime}ms
        """.trimIndent()
        )

        return wrapper
    }

    companion object {
        fun convert(raw: String): MinecraftBedrockInfo {
            val split = raw.split(";")
            val motd: String = split[1].replace(colorCodeRegex, "")
            val agreement = split[2]
            val version = split[3]
            val online = split[4]
            val max = split[5]
            val gameMode = split[8]

            return MinecraftBedrockInfo(version, online, max, gameMode, agreement, motd)
        }
    }
}