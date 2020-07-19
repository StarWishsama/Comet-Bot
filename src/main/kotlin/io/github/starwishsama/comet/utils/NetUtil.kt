package io.github.starwishsama.comet.utils

import cn.hutool.http.HttpException
import cn.hutool.http.HttpRequest
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.Comet
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import java.io.*
import java.net.Proxy
import java.net.Socket
import java.net.SocketTimeoutException

suspend fun InputStream.uploadAsImageSafely(type: String, contact: Contact): Image? {
    try {
        if (type.contains("image")) {
            return this.uploadAsImage(contact)
        }
    } catch (e: Exception) {
        Comet.logger.warning("[网络] 在尝试上传图片时发生了问题", e)
    }
    return null
}

object NetUtil {
    private const val defaultUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36"

    fun getUrlInputStream(url: String): InputStream {
        return getUrlInputStream(url, 8000)
    }

    fun getUrlInputStream(url: String, timeout: Int): InputStream {
        val request = doHttpRequest(url, timeout)

        return request.execute().bodyStream()
    }

    @Throws(HttpException::class)
    fun doHttpRequest(url: String, timeout: Int): HttpRequest {
        val request = HttpRequest.get(url)
                .setFollowRedirects(true)
                .timeout(timeout)
                .header("user-agent", defaultUA)
        if (BotVariables.cfg.proxyUrl != null && BotVariables.cfg.proxyPort != -1) {
            request.setProxy(
                    Proxy(
                            Proxy.Type.HTTP,
                            Socket(BotVariables.cfg.proxyUrl, BotVariables.cfg.proxyPort).remoteSocketAddress
                    )
            )
        }

        return request
    }

    fun getPageContent(url: String): String {
        val response = doHttpRequest(url, 8000).executeAsync()
        return if (response.isOk) response.body() else response.status.toString()
    }

    /**
     * 下载文件
     *
     * @param address  下载地址
     * @param fileName 下载文件的名称
     */
    fun downloadFile(address: String, fileFolder: String, fileName: String) {
        val file = File(fileFolder, fileName)
        try {
            val request = doHttpRequest(address, 8000)
            request.setFollowRedirects(true)
            request.header("User-Agent", defaultUA)

            val response = request.executeAsync()

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
                Comet.logger.error("在下载时发生了错误, 响应码 ${response.status}")
            }
        } catch (e: Exception) {
            if (!file.delete()) {
                Comet.logger.error("无法删除损坏文件: $fileName")
            }
            if (e.cause is SocketTimeoutException) {
                Comet.logger.error("在下载时发生了错误: 连接超时")
                return
            }
            Comet.logger.error("在下载时发生了错误")
        }
    }

    fun downloadFileToCache(url: String): File? {
        return null
    }
}