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
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.search.SearchUserResult
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object SearchApi : ApiExecutor {
    val searchApiService: ISearchApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.bilibili.com/")
            .addConverterFactory(JacksonConverterFactory.create(CometVariables.mapper))
            .client(CometVariables.client)
            .build()
        searchApiService = retrofit.create(ISearchApi::class.java)
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 3000
}

interface ISearchApi {
    @GET("/x/web-interface/search/type")
    fun searchUser(
        @Query("search_type")
        searchType: String = "bili_user",

        @Query("keyword")
        keyword: String
    ): Call<SearchUserResult>
}