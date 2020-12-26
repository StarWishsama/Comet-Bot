package io.github.starwishsama.comet.api.thirdparty.bilibili

import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.video.VideoInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object VideoApi: ApiExecutor {
    private val videoService: IVideoApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        videoService = retrofit.create(IVideoApi::class.java)
    }
    override var usedTime: Int = 0

    override fun getLimitTime(): Int = 2500
}

interface IVideoApi {
    @GET("/x/web-interface/view")
    fun getVideoInfo(@Query("aid") aid: Long): Call<VideoInfo>
}