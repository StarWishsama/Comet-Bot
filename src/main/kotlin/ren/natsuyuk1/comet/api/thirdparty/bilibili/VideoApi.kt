/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.bilibili

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.video.VideoInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object VideoApi : ApiExecutor {
    val videoService: IVideoApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.bilibili.com/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .client(CometVariables.client)
            .build()
        videoService = retrofit.create(IVideoApi::class.java)
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 3500
}

interface IVideoApi {
    @GET("/x/web-interface/view")
    fun getVideoInfo(@Query("aid") aid: String): Call<VideoInfo>

    @GET("/x/web-interface/view")
    fun getVideoInfoByBID(@Query("bvid") bvID: String): Call<VideoInfo>
}