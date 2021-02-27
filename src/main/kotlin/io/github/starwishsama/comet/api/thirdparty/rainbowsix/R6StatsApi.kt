package io.github.starwishsama.comet.api.thirdparty.rainbowsix

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.rainbowsix.data.R6StatsGenericStat
import io.github.starwishsama.comet.api.thirdparty.rainbowsix.data.R6StatsSeasonalStat
import io.github.starwishsama.comet.api.thirdparty.rainbowsix.data.Region
import io.github.starwishsama.comet.api.thirdparty.rainbowsix.data.SeasonName
import io.github.starwishsama.comet.exceptions.ApiKeyIsEmptyException
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

object R6StatsApi: ApiExecutor {
    private val api: IR6StatsAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api2.r6stats.com/public-api/stats/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(BotVariables.client)
            .build()

        api = retrofit.create(IR6StatsAPI::class.java)
    }

    override var usedTime: Int = 0
    override val duration: Int = 1

    override fun getLimitTime(): Int = 60

    fun getR6StatsAPI(): IR6StatsAPI {
        if (cfg.r6StatsKey == null) {
            throw ApiKeyIsEmptyException("未填写 R6Stats API, 无法调用 API")
        }

        checkRateLimit("R6Stats API 调用已达上限")
        usedTime++
        return api
    }

    fun getPlayerStat(userName: String, platform: String = "pc"): MessageWrapper {
        val genericStat: R6StatsGenericStat?
        val seasonalStat: R6StatsSeasonalStat?

        try {
            genericStat = getR6StatsAPI().getGenericInfo(userName, platform).execute().body()
            seasonalStat = getR6StatsAPI().getSeasonalInfo(userName, platform).execute().body()
        } catch (e: Exception) {
            return if (e is ApiKeyIsEmptyException) {
                MessageWrapper().addText("R6Stats API 未正确配置, 请联系机器人管理员")
            } else {
                daemonLogger.warning("R6Stats API 异常", e)
                MessageWrapper().addText("无法获取玩家 $userName 的信息")
            }
        }

        if (genericStat == null || seasonalStat == null) {
            daemonLogger.warning("R6Stats API 异常, genericStat: ${genericStat == null}, seasonalStat: ${seasonalStat == null}")
            return MessageWrapper().addText("无法获取玩家 $userName 的信息")
        }

        val latestSeasonalStat = seasonalStat.getSeasonalStat(SeasonName.NEON_DAWN)?.getRegionStat(Region.EMEA) ?: return MessageWrapper().addText("无法获取玩家 $userName 的信息")

        val infoText = "|| ${genericStat.username} [${genericStat.levelInfo.level} 级]\n" +
                "|| 目前段位 ${latestSeasonalStat.getRank().rankName}\n" +
                "|| MMR 状态 ${latestSeasonalStat.currentMMR} (${latestSeasonalStat.lastMatchMMRChange})\n" +
                "|| KD ${genericStat.stats.generalStat.kd} / WL ${genericStat.stats.generalStat.winLoss}\n" +
                "|| 胜率 ${(genericStat.stats.generalStat.winTime / genericStat.stats.generalStat.playedGameTime) * 100}%"

        return MessageWrapper().addText(infoText)
    }
}

interface IR6StatsAPI {
    @GET("{username}/{platform}/generic")
    fun getGenericInfo(
        @Path("username") userName: String,
        @Path("platform") platform: String = "pc",
        @HeaderMap headerMap: Map<String, String> = mapOf(Pair("Authorization", "Bearer ${cfg.r6StatsKey}"))
    ): Call<R6StatsGenericStat>

    @GET("{username}/{platform}/seasonal")
    fun getSeasonalInfo(
        @Path("username") userName: String,
        @Path("platform") platform: String = "pc",
        @HeaderMap headerMap: Map<String, String> = mapOf(Pair("Authorization", "Bearer ${cfg.r6StatsKey}"))
    ): Call<R6StatsSeasonalStat>
}