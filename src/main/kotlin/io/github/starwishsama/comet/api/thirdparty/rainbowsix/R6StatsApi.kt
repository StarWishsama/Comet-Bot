/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.rainbowsix

import de.jan.r6statsjava.R6Stats
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.exceptions.ApiKeyIsEmptyException
import io.github.starwishsama.comet.managers.ApiManager
import io.github.starwishsama.comet.objects.config.api.R6StatsConfig
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

object R6StatsApi : ApiExecutor {
    private val api: R6Stats

    init {
        val token = ApiManager.getConfig<R6StatsConfig>().token

        if (token.isEmpty()) {
            throw ApiKeyIsEmptyException("未填写 R6Stats API, 无法调用 API")
        }

        api = R6Stats(token)
    }

    override var usedTime: Int = 0
    override val duration: Int = 1

    override fun getLimitTime(): Int = 60

    private fun getR6StatsAPI(): R6Stats {
        if (ApiManager.getConfig<R6StatsConfig>().token.isEmpty()) {
            throw ApiKeyIsEmptyException("未填写 R6Stats API, 无法调用 API")
        }

        checkRateLimit("R6Stats API 调用已达上限")
        usedTime++
        return api
    }

    fun getPlayerStat(userName: String, platform: String = "pc"): MessageWrapper =
        runCatching<MessageWrapper> {
            val playerStat = getR6StatsAPI().getR6PlayerStats(userName, R6Stats.Platform.valueOf(platform))

            val seasonalStat = getR6StatsAPI().getR6PlayerSeasonalStats(userName, R6Stats.Platform.valueOf(platform))
                .getSeason("crime")

            val infoText = "|| ${playerStat.username} [${playerStat.level} 级]\n" +
                    "|| 目前段位 ${seasonalStat.rankText}\n" +
                    "|| MMR 状态 ${seasonalStat.mmr} (${seasonalStat.lastMatchMMRChange})\n" +
                    "|| KD ${playerStat.generalStats.kd} / WL ${playerStat.generalStats.wl}\n" +
                    "|| 胜率 ${
                        String.format(
                            "%.2f",
                            (playerStat.generalStats.wins / playerStat.generalStats.gamesPlayed) * 100
                        )
                    }%"

            return MessageWrapper().addPictureByURL(playerStat.avatarURL146).addText(infoText)
        }.onFailure {
            return MessageWrapper().addText("无法获取玩家 $userName 的信息, 服务器异常")
        }.getOrThrow()
}

enum class SeasonName(val season: String) {

    CRYSTAL_GUARD("crystal_guard"),

    NORTH_STAR("north_star"),

    CRIMSON_HEIST("crimson_heist"),

    NEON_DAWN("neon_dawn"),

    SHADOW_LEGACY("shadow_legacy"),

    STEEL_WAVE("steel_wave"),

    VOID_EDGE("void_edge"),

    SHIFTING_TIDES("shifting_tides"),

    EMBER_RISE("ember_rise")
}