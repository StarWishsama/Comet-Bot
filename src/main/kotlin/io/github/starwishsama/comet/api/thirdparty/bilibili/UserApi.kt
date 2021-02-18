package io.github.starwishsama.comet.api.thirdparty.bilibili

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object UserApi: ApiExecutor {
    val userApiService: IUserApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.bilibili.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(BotVariables.client)
            .build()
        userApiService = retrofit.create(IUserApi::class.java)
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 2500
}

interface IUserApi {
    @GET("x/space/acc/info")
    fun getMemberInfoById(@Query("mid") id: Long): Call<UserInfo>
}