/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserInfo
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserVideoInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object UserApi : ApiExecutor {
    val userApiService: IUserApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.bilibili.com/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .client(CometVariables.client)
            .build()
        userApiService = retrofit.create(IUserApi::class.java)
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 2500
}

interface IUserApi {
    @GET("/x/web-interface/card")
    fun getMemberInfoById(@Query("mid") id: Long): Call<UserInfo>

    @GET("/x/space/arc/search")
    fun getMemberVideoById(
        @Query("mid") id: Long,
        /**
         * 排序方式
         *
         * 默认为pubdate
         * 最新发布：pubdate
         * 最多播放：click
         * 最多收藏：stow
         */
        @Query("order") order: String = "pubdate",
        //@Query("pn") pageNumber: Int,
        //@Query("ps") pageSize: Int
    ): Call<UserVideoInfo>
}