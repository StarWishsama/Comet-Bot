package io.github.starwishsama.comet.api.thirdparty.bilibili

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.video.VideoInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object VideoApi: ApiExecutor {
    val videoService: IVideoApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.bilibili.com/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .client(BotVariables.client)
            .build()
        videoService = retrofit.create(IVideoApi::class.java)
    }
    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 2500
}

interface IVideoApi {
    @GET("/x/web-interface/view")
    fun getVideoInfo(@Query("aid") aid: String): Call<VideoInfo>

    @GET("/x/web-interface/view")
    fun getVideoInfoByBID(@Query("bvid") bvID: String): Call<VideoInfo>
}