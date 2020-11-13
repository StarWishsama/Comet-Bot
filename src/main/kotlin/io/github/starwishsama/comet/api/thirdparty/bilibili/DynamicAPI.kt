package io.github.starwishsama.comet.api.thirdparty.bilibili

import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Card
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DynamicAPI {
    @GET("dynamic_svr/v1/dynamic_svr/get_dynamic_detail")
    fun getDynamicById(@Query("dynamic_id") id: Long): Call<Card>

    @GET("dynamic_svr/v1/dynamic_svr/space_history")
    fun getDynamicTimeline(
            @Query("visitor_uid") visitor: Int = 0,
            @Query("host_uid") host: Int,
            @Query("offset_dynamic_id") offset: Int = 0,
            @Query("need_top") needTop: Int = 0
    ): Call<List<Card>>
}