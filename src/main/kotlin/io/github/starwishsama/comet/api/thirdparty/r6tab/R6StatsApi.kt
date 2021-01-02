package io.github.starwishsama.comet.api.thirdparty.r6tab

import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.exceptions.ApiKeyIsEmptyException
import io.github.starwishsama.comet.objects.pojo.rainbowsix.R6TabGenericStat
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
            .baseUrl("https://api2.r6stats.com/public-api/stats")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(IR6StatsAPI::class.java)
    }

    override var usedTime: Int = 0
    override val duration: Int = 1

    override fun getLimitTime(): Int = 60

    fun getR6StatsAPI(): IR6StatsAPI {
        if (cfg.r6StatsKey == null) {
            throw ApiKeyIsEmptyException("未填写 R6Stats API")
        }

        checkRateLimit("R6Stats API 调用已达上限")
        return api
    }
}

interface IR6StatsAPI {
    @GET("{username}/{platform}/generic")
    fun getGenericInfo(@Path("username") userName: String, @Path("platform") platform: String = "pc", @HeaderMap headerMap: Map<String, String> = mapOf(Pair("Authorization", "Bearer ${cfg.r6StatsKey}"))): Call<R6TabGenericStat>

    @GET("{username}/{platform}/seasonal")
    fun getSeasonalInfo(@Path("username") userName: String, @Path("platform") platform: String = "pc", @HeaderMap headerMap: Map<String, String> = mapOf(Pair("Authorization", "Bearer ${cfg.r6StatsKey}"))): Call<R6TabGenericStat>
}