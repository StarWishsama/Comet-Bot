package io.github.starwishsama.comet.api.thirdparty.penguinstats

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

object PenguinStats

enum class ArkNightServer {
    CN, US, JP, KR
}

interface PenguinStatsAPI {
    @GET("https://penguin-stats.io/PenguinStats/api/v2/result/matrix")
    fun getMatrix(
        @Query("stageFilter")
        stageFilter: List<Long>,
        @Query("itemFilter")
        itemFilter: List<Long>,
        @Query("server")
        serverName: String = ArkNightServer.CN.toString(),
        @Query("show_closed_zones")
        showClosedZone: Boolean = false,
        @Query("is_personal")
        isPersonal: Boolean = false
    ): Call<okhttp3.Response>
}