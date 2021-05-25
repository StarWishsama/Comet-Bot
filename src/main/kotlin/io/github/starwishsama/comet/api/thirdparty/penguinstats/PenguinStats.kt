/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.penguinstats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.penguinstats.data.MatrixResponse
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object PenguinStats {
    val api: PenguinStatsAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://penguin-stats.io/PenguinStats/api/v2/")
            .addConverterFactory(JacksonConverterFactory.create(BotVariables.mapper))
            .client(BotVariables.client)
            .build()

        api = retrofit.create(PenguinStatsAPI::class.java)
    }
}

enum class ArkNightServer {
    CN, US, JP, KR
}

interface PenguinStatsAPI {
    @GET("result/matrix")
    fun getMatrix(
        @Query("stageFilter")
        stageFilter: List<Long> = listOf(),
        @Query("itemFilter")
        itemFilter: List<String> = listOf(),
        @Query("server")
        serverName: String = ArkNightServer.CN.toString(),
        @Query("show_closed_zones")
        showClosedZone: Boolean = false,
        @Query("is_personal")
        isPersonal: Boolean = false
    ): MatrixResponse
}