/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils.network

import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.CometVariables.netLogger
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.service.RetrofitLogger
import io.github.starwishsama.comet.utils.StringUtil.containsEtc
import io.github.starwishsama.comet.utils.TaskUtil
import okhttp3.*
import retrofit2.HttpException
import java.io.*
import java.net.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

fun Response.isType(typeName: String): Boolean = headers["content-type"]?.contains(typeName) == true

object NetUtil {
    const val defaultUA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36 Edg/94.0.992.50"

    const val cometRequestUA =
        "Comet/${BuildConfig.version} AppleWebKit/537.36 (KHTML, like Gecko) Not Chrome/94.0.4606.81"

    /**
     * 执行 Http 请求
     *
     * 注意：响应需要使用 [Response.close] 关闭或使用 [use], 否则会导致泄漏
     *
     * 获取后的 [Response] 为异步, 响应完全读取 (如使用 [Response.body] 获取响应体字符串) 后会失效
     *
     * 并且**不可再使用**, 否则抛出 [IllegalStateException]
     *
     * @param url 请求的地址
     * @param timeout 超时时间, 单位为秒
     * @param proxyUrl 代理地址 (如果需要使用的话)
     * @param proxyPort 代理端口 (如果需要使用的话)
     * @param action 执行请求前的额外操作, 如添加 header 等. 详见 [Request.Builder]
     */
    fun executeRequest(
        url: String,
        timeout: Long = 2,
        proxyUrl: String = cfg.proxyUrl,
        proxyPort: Int = cfg.proxyPort,
        method: String = "GET",
        body: RequestBody? = null,
        action: Request.Builder.() -> Request.Builder = {
            header("user-agent", defaultUA)
        }
    ): Call {
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .followRedirects(true)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(RetrofitLogger())

        try {
            if (checkProxyUsable()) {
                builder.proxy(Proxy(cfg.proxyType, InetSocketAddress(proxyUrl, proxyPort)))
            }
        } catch (e: Exception) {
            netLogger.warning("无法连接到代理服务器, 将使用直连链接\n错误信息: ${e.message}")
        }

        val client = builder.build()
        val request = Request.Builder().url(url).action().also {
            if (method == "POST" && body != null) {
                it.method(method, body)
            }
        }.build()

        return client.newCall(request)
    }

    /**
     * 执行 Http 请求 (Get)
     *
     * 注意：响应需要使用 [Response.close] 关闭或使用 [use], 否则会导致泄漏
     *
     * 获取后的 [Response] 为异步, 响应完全读取后会失效
     *
     * 并且**不可再使用**, 否则抛出 [IllegalStateException]
     *
     * 附上执行后响应会失效的方法：
     * [Response.close]
     * Response.body.close()
     * Response.body().source().close()
     * Response.body().charStream().close()
     * Response.body().byteString().close()
     * Response.body().bytes()
     * Response.body().string()
     *
     * @param url 请求的地址
     * @param timeout 超时时间, 单位为秒
     * @param proxyUrl 代理地址 (如果需要使用的话)
     * @param proxyPort 代理端口 (如果需要使用的话)
     * @param call 执行请求前的额外操作, 如添加 header 等. 详见 [Request.Builder]
     * @param autoClose 是否自动关闭 [ResponseBody], 适用于需要使用 bodyStream 等环境
     * @param autoCloseDelay 自动关闭 [ResponseBody] 的延迟秒数
     */
    fun executeHttpRequest(
        url: String,
        timeout: Long = 10,
        proxyUrl: String = cfg.proxyUrl,
        proxyPort: Int = cfg.proxyPort,
        call: Request.Builder.() -> Request.Builder = {
            header("user-agent", defaultUA)
        },
        autoClose: Boolean = false,
        autoCloseDelay: Long = 15
    ): Response {
        var result: Response? = null

        try {
            result = executeRequest(url, timeout, proxyUrl, proxyPort, action = call).execute()
        } catch (e: IOException) {
            netLogger.warning("执行网络操作失败: $url", e)
        } finally {
            if (autoClose) {
                TaskUtil.schedule(autoCloseDelay) {
                    if (result?.body != null) {
                        result.close()
                    }
                }
            }
        }

        return result ?: throw RuntimeException("执行网络操作失败, 响应为空: $url")
    }

    fun getPageContent(
        url: String, timeout: Long = 2, proxyUrl: String = cfg.proxyUrl, proxyPort: Int = cfg.proxyPort,
        call: Request.Builder.() -> Request.Builder = {
            header("user-agent", defaultUA)
        },
    ): String? {
        return try {
            executeHttpRequest(url, timeout, proxyUrl, proxyPort, call).body?.string()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 下载文件
     *
     * @param address  下载地址
     * @param downloadPath 下载文件储存的文件夹
     */
    private fun downloadFile(downloadPath: File, address: String): File {
        val url = URL(address)
        val conn = url.openConnection() as HttpURLConnection

        try {
            conn.doOutput = true
            conn.instanceFollowRedirects = true
            conn.connect()

            if (conn.responseCode in 200..300) {
                val `in` = BufferedInputStream(conn.inputStream)
                if (!downloadPath.exists()) downloadPath.createNewFile()
                val fos = FileOutputStream(downloadPath)
                val bos = BufferedOutputStream(fos, 2048)
                val data = ByteArray(2048)
                var x: Int
                while (`in`.read(data, 0, 2048).also { x = it } >= 0) {
                    bos.write(data, 0, x)
                }
                bos.close()
                `in`.close()
                fos.close()
            } else {
                throw ApiException("在下载时发生了错误, 响应码 ${conn.responseCode}")
            }
        } finally {
            conn.disconnect()
        }

        return downloadPath
    }

    /**
     * 下载文件
     *
     * @param address 下载地址
     * @param downloadPath 下载文件储存的文件夹
     * @param fileName 下载文件名
     */
    fun downloadFile(downloadPath: File, address: String, fileName: String): File {
        return downloadFile(File(downloadPath, fileName), address)
    }

    fun isTimeout(t: Throwable): Boolean {
        if (t is CancellationException) {
            return false
        }

        val msg = t.message?.lowercase() ?: return false
        return msg.containsEtc(true, "time", "out") || t.javaClass.simpleName.lowercase().contains("timeout")
    }

    @Throws(HttpException::class)
    fun checkPingValue(address: String = "https://www.gstatic.com/generate_204", timeout: Long = 2000): Long {
        val startTime = LocalDateTime.now()

        executeHttpRequest(address, timeout).use { conn ->
            return if (conn.isSuccessful) {
                Duration.between(startTime, LocalDateTime.now()).toMillis()
            } else {
                -1L
            }
        }
    }

    fun getInputStream(url: String, timeout: Long = 3): InputStream? {
        val body = executeHttpRequest(url = url, timeout = timeout, autoClose = true, autoCloseDelay = 30).body
        return body?.byteStream()
    }

    fun checkProxyUsable(url: String = "https://www.gstatic.com/generate_204", timeout: Int = 2_000): Boolean {
        if (!cfg.proxySwitch || cfg.proxyUrl.isEmpty() || cfg.proxyPort <= 0) return false

        Socket(cfg.proxyUrl, cfg.proxyPort).use {
            try {
                val connection =
                    (URL(url).openConnection(Proxy(cfg.proxyType, it.remoteSocketAddress))) as HttpURLConnection
                connection.connectTimeout = timeout
                connection.connect()

            } catch (ignored: IOException) {
                return false
            }
            return it.inetAddress.isReachable(timeout)
        }
    }

    fun getRedirectedURL(origin: String): String? {
        executeHttpRequest(origin).use { request ->
            return request.priorResponse?.headers?.get("location")
        }
    }
}