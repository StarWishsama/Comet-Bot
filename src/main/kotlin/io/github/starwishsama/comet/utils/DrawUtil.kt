package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
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

    /**
     * 根据抽卡结果合成图片
     */
    fun combineArkOpImage(list: List<ArkNightOperator>): BufferedImage {
        /**
         * 缩小图片大小，减少流量消耗
         */
        val zoom = 2
        val height = 728 / zoom

        val newBufferedImage: BufferedImage = if (list.size == 1) {
            BufferedImage(256 / zoom, height, BufferedImage.TYPE_INT_RGB)
        } else {
            BufferedImage(2560 / zoom, height, BufferedImage.TYPE_INT_RGB)
        }

        val createGraphics = newBufferedImage.createGraphics()

        var newBufferedImageWidth = 0
        val newBufferedImageHeight = 0

        for (i in list) {
            val file = File(FileUtil.getChildFolder("res${File.separator}" + i.rare), i.name + ".jpg")
            val inStream: InputStream = file.inputStream()

            val bufferedImage: BufferedImage = ImageIO.read(inStream)

            val imageWidth = bufferedImage.width / zoom
            val imageHeight = bufferedImage.height / zoom

            createGraphics.drawImage(
                bufferedImage.getScaledInstance(
                    imageWidth,
                    imageHeight,
                    java.awt.Image.SCALE_SMOOTH
                ), newBufferedImageWidth, newBufferedImageHeight, imageWidth, imageHeight, null
            )

            newBufferedImageWidth += imageWidth

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
            user.commandTime >= time || user.compareLevel(UserLevel.ADMIN) && time <= 10000
}