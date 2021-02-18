package io.github.starwishsama.comet.logger

import io.github.starwishsama.comet.BotVariables.netLogger
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class RetrofitLogger: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        netLogger.log(HinaLogLevel.Debug,
            "发送请求 [${request.method}] ${request.url}",
            prefix = "网络"
        )

        val start = System.nanoTime()
        val response = chain.proceed(request)
        val end = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)

        netLogger.log(HinaLogLevel.Debug,
            "收到响应 ${request.url}, 耗时 ${end}ms",
            prefix = "网络"
        )
        return response
    }
}