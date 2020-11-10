package io.github.starwishsama.comet.utils

import cn.hutool.core.net.URLEncoder
import io.github.starwishsama.comet.BotVariables.arkNight
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
import io.github.starwishsama.comet.utils.StringUtil.getLastingTime
import io.github.starwishsama.comet.utils.network.NetUtil
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.time.LocalDateTime
import javax.imageio.ImageIO

object DrawUtil {
    /**
     * 明日方舟
     */

    const val overTimeMessage = "抽卡次数到上限了, 可以少抽一点或者等待条数自动恢复哦~\n" +
            "命令条数现在每小时会恢复100次, 封顶1000次"

    // 资源下载链接, 文件位置: 干员名称/干员名称.png
    private const val resourceUrl = "https://cdn.jsdelivr.net/gh/godofhuaye/arknight-assets@master/cg"

    /**
     * 根据抽卡结果合成图片
     */
    fun combineArkOpImage(ops: List<ArkNightOperator>): CombinedResult {
        require(ops.isNotEmpty()) { "传入的干员列表不能为空!" }

        val lostOperators = mutableListOf<ArkNightOperator>()

        val picSize = 180

        val newBufferedImage = BufferedImage(picSize * ops.size, picSize, BufferedImage.TYPE_INT_RGB)

        val createGraphics = newBufferedImage.createGraphics()

        var newBufferedImageWidth = 0

        for (i in ops) {
            val file = File(FileUtil.getResourceFolder().getChildFolder("ark"), i.name + ".png")

            if (!file.exists()) {
                lostOperators.plusAssign(i)
                daemonLogger.warning("明日方舟: 干员 ${i.name} 的图片不存在")
            } else {
                val inStream: InputStream = file.inputStream()

                val bufferedImage: BufferedImage = ImageIO.read(inStream)

                val imageWidth = bufferedImage.width
                val imageHeight = bufferedImage.height

                createGraphics.drawImage(
                        bufferedImage.getScaledInstance(
                                imageWidth,
                                imageHeight,
                                Image.SCALE_SMOOTH
                        ), newBufferedImageWidth, 0, imageWidth, imageHeight, null
                )

                newBufferedImageWidth += imageWidth

            }
        }

        createGraphics.dispose()

        return CombinedResult(newBufferedImage, lostOperators)

    }

    data class CombinedResult(
            val image: BufferedImage,
            val lostOps: List<ArkNightOperator>
    )

    fun getStar(rare: Int): String = buildString {
        for (i in 0 until rare) {
            append("★")
        }
    }

    fun checkHasGachaTime(user: BotUser, time: Int): Boolean =
            (user.commandTime >= time || user.compareLevel(UserLevel.ADMIN)) && time <= 10000

    fun downloadArkNightsFile() {
        // 安塞尔的资源不知为何没有
        val actualSize = arkNight.size - 1

        if (FileUtil.getResourceFolder().getChildFolder("ark").filesCount() < actualSize) {
            val startTime = LocalDateTime.now()
            daemonLogger.info("正在下载 明日方舟图片资源文件")

            val arkLoc = FileUtil.getResourceFolder().getChildFolder("ark")

            var successCount = 0

            arkNight.forEach {
                val fileName = "${it.name}.png"
                val pathName = URLEncoder.createDefault().encode("${it.name}/$fileName", Charset.forName("UTF-8"))
                try {
                    val file = File(arkLoc, fileName)
                    if (!file.exists()) {
                        val result = TaskUtil.executeRetry(3) {
                            NetUtil.downloadFile(arkLoc, "$resourceUrl/$pathName", fileName)
                            successCount++
                        }
                        if (result != null) throw result
                    }
                } catch (e: Exception) {
                    if (e !is ApiException)
                        daemonLogger.warning("下载 $fileName 时出现了意外", e)
                    else
                        daemonLogger.warning("下载异常: ${e.message ?: "无信息"}")
                    return@forEach
                } finally {
                    if (successCount > 0 && successCount % 10 == 0) {
                        daemonLogger.info("明日方舟 > 已下载 $successCount/${arkNight.size}")
                    }
                }
            }
            daemonLogger.info("明日方舟 > 资源文件下载成功 [$successCount/${actualSize}], 耗时 ${startTime.getLastingTime()}")
        }
    }
}