package io.github.starwishsama.comet.logger

import io.github.starwishsama.comet.BotVariables.netLogger
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class RetrofitLogger: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        netLogger.debug(
            "发送请求 [${request.method}] ${request.url}"
        )

        val start = System.nanoTime()
        val response = chain.proceed(request)
        val end = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)

        netLogger.debug("收到响应 ${request.url}, 耗时 ${end}ms")
        return response
    }
}