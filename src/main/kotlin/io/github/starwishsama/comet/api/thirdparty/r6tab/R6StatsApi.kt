package io.github.starwishsama.comet.api.thirdparty.r6tab

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

object R6StatsApi {
    val api: IR6StatsAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api2.r6stats.com/public-api/stats")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(IR6StatsAPI::class.java)
    }
}

interface IR6StatsAPI {
    @GET("{username}/{platform}/generic")
    fun getGenericInfo(@Path("username") userName: String, @Path("platform") platform: String)
}