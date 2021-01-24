package io.github.starwishsama.comet.utils.network

import cn.hutool.http.HttpException
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.utils.StringUtil.containsEtc
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.verboseS
import okhttp3.*
import java.io.*
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.Socket
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

fun Response.isType(typeName: String): Boolean = headers["content-type"]?.contains(typeName) == true

object NetUtil {
    const val defaultUA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36"

    /**
     * 执行 Http 请求 (Get)
     *
     * 注意：响应需要使用 [Response.close] 关闭或使用 [use], 否则会导致泄漏
     *
     * 获取后的 [Response] 为异步, 响应完全读取 (如使用 [Response.body] 获取响应体字符串)后会失效
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
        action: Request.Builder.() -> Request.Builder = {
            header("user-agent", defaultUA)
        }
    ): Call {
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .followRedirects(true)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }


        try {
            if (checkProxyUsable()) {
                builder.proxy(Proxy(cfg.proxyType, Socket(proxyUrl, proxyPort).remoteSocketAddress))
                daemonLogger.verbose("使用代理连接")
            }
        } catch (e: Exception) {
            daemonLogger.info("无法连接到代理服务器, ${e.message}")
        }

        val client = builder.build()
        val request = Request.Builder().url(url).action().build()
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
        timeout: Long = 2,
        proxyUrl: String = cfg.proxyUrl,
        proxyPort: Int = cfg.proxyPort,
        call: Request.Builder.() -> Request.Builder = {
            header("user-agent", defaultUA)
        },
        autoClose: Boolean = false,
        autoCloseDelay: Long = 15
    ): Response {
        val startTime = System.nanoTime()

        var result: Response? = null

        try {
            result = executeRequest(url, timeout, proxyUrl, proxyPort, call).execute()
        } catch (e: IOException) {
            daemonLogger.warning("执行网络操作失败\n" + e.stackTraceToString())
        } finally {
            daemonLogger.verboseS("执行网络操作用时 ${(System.nanoTime() - startTime).toDouble() / 1_000_000}ms ($url)")

            if (autoClose) {
                TaskUtil.runAsync(autoCloseDelay) {
                    if (result?.body != null) {
                        result.close()
                    }
                }
            }
        }

        return result ?: throw ApiException("执行网络操作失败")
    }

    fun getPageContent(url: String, timeout: Long = 2): String? =
        executeHttpRequest(url, timeout, cfg.proxyUrl, cfg.proxyPort).body?.string()

    /**
     * 下载文件
     *
     * @param address  下载地址
     * @param downloadPath 下载文件储存的文件夹
     */
    fun downloadFile(downloadPath: File, address: String): File {
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
        val msg = t.message?.toLowerCase() ?: return false
        // FIXME: 这不是一个很好的识别方法
        return msg.containsEtc(false, "time", "out") || t.javaClass.simpleName.toLowerCase().contains("timeout")
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

    fun checkProxyUsable(customUrl: String = "https://www.gstatic.com/generate_204", timeout: Int = 2_000): Boolean {
        if (!cfg.proxySwitch || cfg.proxyUrl.isEmpty() || cfg.proxyPort <= 0) return false

        Socket(cfg.proxyUrl, cfg.proxyPort).use {
            try {
                val connection =
                    (URL(customUrl).openConnection(Proxy(cfg.proxyType, it.remoteSocketAddress))) as HttpURLConnection
                connection.connectTimeout = 2000
                connection.connect()

            } catch (t: IOException) {
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