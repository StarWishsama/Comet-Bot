package io.github.starwishsama.comet.logger

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.utils.debugS
import io.github.starwishsama.comet.utils.warningS
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class RetrofitLogger: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        daemonLogger.warningS(
            "发送请求 [${request.method}] ${request.url}"
        )

        val start = System.nanoTime()
        val response = chain.proceed(request)
        val end = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)

        daemonLogger.debugS("收到响应 ${request.url}, 耗时 ${end}ms")
        return response
    }
}