package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.BotVariables.arkNight
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
import io.github.starwishsama.comet.utils.network.NetUtil
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

object DrawUtil {
    /**
     * 明日方舟
     */

    const val overTimeMessage = "抽卡次数到上限了, 可以少抽一点或者等待条数自动恢复哦~\n" +
            "命令条数现在每小时会恢复100次, 封顶1000次"

    // 干员名称/干员名称.png
    const val resourceUrl = "https://cdn.jsdelivr.net/gh/godofhuaye/arknight-assets@master/cg/"

    /**
     * 根据抽卡结果合成图片
     */
    fun combineArkOpImage(ops: List<ArkNightOperator>): BufferedImage {
        require(ops.isNotEmpty()) { "传入的干员列表不能为空!" }

        /**
         * 缩小图片大小，减少流量消耗
         */
        val zoom = 2
        val picSize = 180

        val newBufferedImage = BufferedImage(picSize * ops.size, picSize, BufferedImage.TYPE_INT_RGB)

        val createGraphics = newBufferedImage.createGraphics()

        var newBufferedImageWidth = 0
        val newBufferedImageHeight = 0

        for (i in ops) {
            val file = File(FileUtil.getResourceFolder().getChildFolder("ark"), i.name + ".png")

            if (!file.exists()) {
                daemonLogger.warning("明日方舟: 干员 ${i.name} 的图片不存在")
            } else {
                val inStream: InputStream = file.inputStream()

                val bufferedImage: BufferedImage = ImageIO.read(inStream)

                val imageWidth = bufferedImage.width / zoom
                val imageHeight = bufferedImage.height / zoom

                createGraphics.drawImage(
                        bufferedImage.getScaledInstance(
                                imageWidth,
                                imageHeight,
                                Image.SCALE_SMOOTH
                        ), newBufferedImageWidth, newBufferedImageHeight, imageWidth, imageHeight, null
                )

                newBufferedImageWidth += imageWidth

            }
        }

        createGraphics.dispose()

        return newBufferedImage

    }

    fun getStar(rare: Int): String = StringBuilder().apply {
        for (i in 0 until rare) {
            append("★")
        }
    }.toString()

    fun checkHasGachaTime(user: BotUser, time: Int): Boolean =
            (user.commandTime >= time || user.compareLevel(UserLevel.ADMIN)) && time <= 10000

    fun downloadArkNightsFile() {
        if (FileUtil.getResourceFolder().getChildFolder("ark").isEmpty()) {
            daemonLogger.info("正在下载 明日方舟图片资源文件")
            arkNight.forEach {
                val fileName = "${it.name}.png"
                try {
                    NetUtil.downloadFile(FileUtil.getResourceFolder().getChildFolder("ark"), "$resourceUrl$it.name/$fileName", fileName)
                } catch (e: RuntimeException) {
                    daemonLogger.warning("下载 $fileName 时出现了意外", e)
                    return
                }
            }
            daemonLogger.info("下载明日方舟资源文件完成!")
        }
    }
}