package io.github.starwishsama.comet.utils

import cn.hutool.core.io.IORuntimeException
import cn.hutool.http.HttpException
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import cn.hutool.http.Method
import io.github.starwishsama.comet.BotVariables
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import java.io.*
import java.net.Proxy
import java.net.Socket


suspend fun InputStream.uploadAsImageSafely(type: String, contact: Contact): Image? {
    try {
        if (type.contains("image")) {
            return this.uploadAsImage(contact)
        }
    } catch (e: Exception) {
        BotVariables.logger.warning("[网络] 在尝试上传图片时发生了问题", e)
    }
    return null
}

fun HttpResponse.getContentLength(): Int {
    return header("Content-Length").toIntOrNull() ?: -1
}

object NetUtil {
    const val defaultUA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36"

    fun getUrlInputStream(url: String?): InputStream? {
        if (url == null) return null
        return getUrlInputStream(url, 8000)
    }

    fun getUrlInputStream(url: String, timeout: Int): InputStream? {
        val response = doHttpRequestGet(url, timeout).executeAsync()
        val length = response.getContentLength()
        val bytes = response.bodyBytes()
        if (bytes.size < length) return null

        return ByteArrayInputStream(bytes)
    }

    @Throws(HttpException::class)
    fun doHttpRequestGet(url: String, timeout: Int): HttpRequest {
        return doHttpRequest(url, timeout, BotVariables.cfg.proxyUrl, BotVariables.cfg.proxyPort, Method.GET)
    }

    @Throws(HttpException::class)
    fun doHttpRequest(url: String, timeout: Int, proxyUrl: String, proxyPort: Int, method: Method): HttpRequest {
        val request = HttpRequest(url)
            .method(method)
            .setFollowRedirects(true)
            .timeout(timeout)
            .header("user-agent", defaultUA)

        if (proxyUrl.isNotEmpty() && proxyPort != -1) {
            request.setProxy(
                Proxy(
                    Proxy.Type.HTTP,
                    Socket(proxyUrl, proxyPort).remoteSocketAddress
                )
            )
        }

        return request
    }

    fun getPageContent(url: String): String {
        val response = doHttpRequestGet(url, 8000).executeAsync()
        return if (response.isOk) response.body() else response.status.toString()
    }

    /**
     * 下载文件
     *
     * @param address  下载地址
     * @param fileName 下载文件的名称
     */
    fun downloadFile(fileFolder: File, address: String, fileName: String): File? {
        val file = File(fileFolder, fileName)
        try {
            val response = doHttpRequestGet(address, 5000).executeAsync()

            if (response.isOk) {
                val `in` = BufferedInputStream(response.bodyStream())
                if (!file.exists()) file.createNewFile()
                val fos = FileOutputStream(file)
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
                BotVariables.logger.error("在下载时发生了错误, 响应码 ${response.status}")
            }
        } catch (e: Exception) {
            if (!file.delete()) {
                BotVariables.logger.error("无法删除损坏文件: $fileName")
            }
            if (e.cause is IORuntimeException) {
                BotVariables.logger.error("在下载时发生了错误: 连接超时")
                return null
            }
            BotVariables.logger.error("在下载时发生了错误")
        }

        return file
    }

    fun downloadFileToCache(url: String, fileName: String): File? {
        return downloadFile(FileUtil.getCacheFolder(), url, fileName)
    }
}