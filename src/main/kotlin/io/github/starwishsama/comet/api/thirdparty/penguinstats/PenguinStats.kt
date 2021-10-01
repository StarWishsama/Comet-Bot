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

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.penguinstats.data.ArkNightItemInfo
import io.github.starwishsama.comet.api.thirdparty.penguinstats.data.ArkNightStageInfo
import io.github.starwishsama.comet.api.thirdparty.penguinstats.data.MatrixResponse
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.writeClassToJson
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.IOException

object PenguinStats {
    val api: PenguinStatsAPI
    val itemInfo: MutableSet<ArkNightItemInfo> = mutableSetOf()
    val stageInfo: MutableSet<ArkNightStageInfo> = mutableSetOf()
    private val itemInfoFile = File(FileUtil.getResourceFolder(), "arknight_item.serialize")
    private val stageInfoFile = File(FileUtil.getResourceFolder(), "arknight_stage.serialize")

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://penguin-stats.io/PenguinStats/api/v2/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .client(CometVariables.client)
            .build()

        api = retrofit.create(PenguinStatsAPI::class.java)

        checkCache()
    }

    fun getItemDropInfo(item: String): String {
        val itemInstance = itemInfo.find { it.exists(item) } ?: return "你要搜索的物品不存在: $item"

        return try {
            val matrix = api.getMatrix(itemFilter = listOf(itemInstance.itemId))

            matrix.execute().body()?.toString() ?: "连接至企鹅物流时发生了异常"
        } catch (e: IOException) {
            daemonLogger.warning("连接至企鹅物流时发生了异常", e)
            "连接至企鹅物流时发生了异常"
        }
    }

    private fun checkCache() {
        try {
            if (itemInfoFile.exists()) {
                itemInfo.addAll(mapper.readValue(itemInfoFile))
            } else {
                api.getItemInfo().execute().body()?.let { itemInfo.addAll(it) }
                itemInfoFile.createNewFile()
                itemInfoFile.writeClassToJson(itemInfo)
            }
        } catch (e: IOException) {
            daemonLogger.warning("在尝试获取明日方舟物品信息时出现异常", e)
        }

        try {
            if (stageInfoFile.exists()) {
                stageInfo.addAll(mapper.readValue(stageInfoFile))
            } else {
                api.getStageInfo().execute().body()?.let { stageInfo.addAll(it) }
                stageInfoFile.createNewFile()
                stageInfoFile.writeClassToJson(stageInfo)
            }
        } catch (e: IOException) {
            daemonLogger.warning("在尝试获取明日方舟关卡信息时出现异常", e)
        }
    }

    fun forceUpdate() {
        itemInfo.apply {
            try {
                clear()
                api.getItemInfo().execute().body()?.let { addAll(it) }
                itemInfoFile.writeClassToJson(itemInfo)
            } catch (e: IOException) {
                daemonLogger.warning("在尝试获取明日方舟物品信息时出现异常", e)
            }
        }
        stageInfo.apply {
            try {
                clear()
                api.getStageInfo().execute().body()?.let { addAll(it) }
                stageInfoFile.writeClassToJson(stageInfo)
            } catch (e: IOException) {
                daemonLogger.warning("在尝试获取明日方舟关卡信息时出现异常", e)
            }
        }
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
    ): Call<MatrixResponse>

    @GET("items")
    fun getItemInfo(): Call<MutableSet<ArkNightItemInfo>>

    @GET("stages")
    fun getStageInfo(): Call<MutableSet<ArkNightStageInfo>>
}