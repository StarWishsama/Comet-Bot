/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service

import io.github.starwishsama.comet.CometVariables.netLogger
import io.github.starwishsama.comet.logger.HinaLogLevel
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class RetrofitLogger : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        netLogger.log(
            HinaLogLevel.Debug,
            "发送请求 [${request.method}] ${request.url}",
            prefix = "网络"
        )

        val start = System.nanoTime()
        val response = chain.proceed(request)
        val end = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)

        netLogger.log(
            HinaLogLevel.Debug,
            "收到响应 ${request.url}, 耗时 ${end}ms",
            prefix = "网络"
        )

        return response
    }
}