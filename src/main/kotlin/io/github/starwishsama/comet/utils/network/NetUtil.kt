package io.github.starwishsama.comet.utils.network

import cn.hutool.core.io.IORuntimeException
import cn.hutool.http.*
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.utils.network.NetUtil.proxyIsUsable
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.ie.InternetExplorerDriver
import org.openqa.selenium.opera.OperaDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.*
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.Socket
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

fun HttpResponse.getContentLength(): Int {
    return header(Header.CONTENT_LENGTH).toIntOrNull() ?: -1
}

fun Socket.isUsable(timeout: Int = 1_000, isReloaded: Boolean = false): Boolean {
    if (proxyIsUsable == 0 || isReloaded) {
        try {
            val connection = (URL("https://www.gstatic.com/generate_204").openConnection(Proxy(cfg.proxyType, this.remoteSocketAddress))) as HttpURLConnection
            connection.connectTimeout = 2000
            connection.connect()
            proxyIsUsable = 1
        } catch (t: IOException) {
            proxyIsUsable = -1
        } finally {
            close()
        }
    }
    return inetAddress.isReachable(timeout)
}

fun HttpResponse.isType(typeName: String): Boolean {
    val contentType = this.header("content-type") ?: return true
    return contentType.contains(typeName)
}

object NetUtil {
    var proxyIsUsable = 0
    lateinit var driver: WebDriver

    const val defaultUA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36"

    fun getUrlInputStream(url: String, timeout: Int = 4_000): InputStream? {
        val response = doHttpRequestGet(url, timeout).executeAsync()
        val length = response.getContentLength()
        val bytes = response.bodyBytes()
        if (bytes.size < length) return null

        return ByteArrayInputStream(bytes)
    }

    @Throws(HttpException::class)
    fun doHttpRequestGet(url: String, timeout: Int = 4_000): HttpRequest {
        return doHttpRequest(url, timeout, cfg.proxyUrl, cfg.proxyPort, Method.GET)
    }

    @Throws(HttpException::class)
    fun doHttpRequest(url: String, timeout: Int, proxyUrl: String, proxyPort: Int, method: Method): HttpRequest {
        val request = HttpRequest(url)
                .method(method)
                .setFollowRedirects(true)
                .timeout(timeout)
                .header("user-agent", defaultUA)


        if (proxyIsUsable >= 0) {
            try {
                val socket = Socket(proxyUrl, proxyPort)
                if (socket.isUsable()) {
                    request.setProxy(Proxy(cfg.proxyType, Socket(proxyUrl, proxyPort).remoteSocketAddress))
                }
            } catch (e: Exception) {
                daemonLogger.verbose("无法连接到代理服务器, ${e.message}")
            }
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

    fun isTimeout(t: Throwable): Boolean {
        val msg = t.message?.toLowerCase(Locale.ROOT) ?: return false
        // FIXME: 这不是一个很好的识别方法
        return (msg.contains("time") && msg.contains("out")) || t.javaClass.simpleName.toLowerCase(Locale.ROOT).contains("timeout")
    }

    @Throws(HttpException::class)
    fun checkPingValue(address: String = "https://www.gstatic.com/generate_204", timeout: Int = 3000): Long {
        val startTime = LocalDateTime.now()

        val conn = doHttpRequestGet(address, timeout).executeAsync()
        return if (conn.isOk) {
            Duration.between(startTime, LocalDateTime.now()).toMillis()
        } else {
            -1L
        }
    }

    /**
     * 获取网页截图
     *
     * @param address 需截图的网页地址
     * @param executeScript 执行的额外操作, 如执行脚本
     */
    fun getScreenshot(
        address: String, executeScript: WebDriver.() -> Unit = {
            val wait = WebDriverWait(this, 10, 1)

            // 等待推文加载完毕再截图
            wait.until(ExpectedCondition { webDriver ->
                webDriver?.findElement(By.cssSelector("article"))
            })

            // 执行脚本获取合适的推文宽度
            val jsExecutor = (this as JavascriptExecutor)
            val width =
                jsExecutor.executeScript("""return document.querySelector("section").getBoundingClientRect().bottom""") as Double

            // 调整窗口大小
            manage().window().size = Dimension(640, width.toInt())
        }
    ): File? {
        try {
            var isConnected = false

            // 检查驱动器是否正常, 如果不正常重新初始化
            try {
                driver.get(address)
                isConnected = true
            } catch (e: WebDriverException) {
                initDriver()
            }

            // 避免重新获取徒增时长
            if (isConnected) driver.get(address)

            driver.executeScript()

            return (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
        } catch (e: Exception) {
            daemonLogger.warning("在调用 WebDriver 时出现异常", e)
        }

        return null
    }

    fun initDriver() {
        var driverName = cfg.webDriverName

        try {
            driver = when (driverName.toLowerCase()) {
                "chrome" -> ChromeDriver()
                "edge" -> EdgeDriver()
                "firefox" -> FirefoxDriver()
                "ie", "internetexplorer" -> InternetExplorerDriver()
                "opera" -> OperaDriver()
                else -> {
                    if (driverName.isNotEmpty()) {
                        daemonLogger.warning("不支持的 WebDriver 类型: ${driverName}, Comet 支持 [Chrome, Edge, Firefox, IE, Opera]")
                        return
                    }
                    driverName = "Remote"
                    RemoteWebDriver(URL(cfg.remoteWebDriver), DesiredCapabilities.chrome())
                }
            }
        } catch (e: RuntimeException) {
            daemonLogger.warning("在尝试加载 WebDriver for $driverName 时出现问题", e)
        }
    }
}