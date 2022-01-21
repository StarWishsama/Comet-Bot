/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.utils

import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.api.gacha.impl.ArkNightInstance.pictureReady
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.gacha.items.GachaItem
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.objects.gacha.pool.GachaPool
import io.github.starwishsama.comet.service.gacha.GachaConstants

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

object GachaUtil {
    const val overTimeMessage = "抽卡次数到上限了, 可以少抽一点或者等待条数自动恢复哦~\n" +
            "命令条数现在每小时会恢复100次, 封顶1000次"

    @Suppress("UNCHECKED_CAST")
    fun combineGachaImage(gachaResult: List<GachaItem>, poolType: GachaPool): CombinedResult {
        require(gachaResult.isNotEmpty()) { "传入的抽卡结果列表不能为空!" }

        return when (poolType) {
            is ArkNightPool -> combineArkOpImage(gachaResult as List<ArkNightOperator>)
            else -> throw UnsupportedOperationException("暂不支持合成该卡池图片")
        }
    }

    /**
     * 根据抽卡结果合成图片
     */
    private fun combineArkOpImage(ops: List<ArkNightOperator>): CombinedResult {
        require(ops.isNotEmpty()) { "传入的干员列表不能为空!" }

        val lostOperators = mutableListOf<ArkNightOperator>()

        val picSize = 180

        val picHeight = 380

        val gachaResultImage = BufferedImage(picSize * ops.size, picHeight, BufferedImage.TYPE_INT_RGB)

        val graphics = gachaResultImage.createGraphics()

        var newBufferedImageWidth = 0

        for (i in ops) {
            val file = File(FileUtil.getResourceFolder().getChildFolder("ark"), i.name + ".png")

            if (!file.exists()) {
                lostOperators.add(i)
                daemonLogger.warning("明日方舟: 干员 ${i.name} 的图片不存在")
            } else {
                val inStream: InputStream = file.inputStream()

                val bufferedImage: BufferedImage = ImageIO.read(inStream)

                val imageWidth = bufferedImage.width
                val imageHeight = bufferedImage.height

                graphics.drawImage(
                    bufferedImage.getScaledInstance(
                        imageWidth,
                        imageHeight,
                        Image.SCALE_SMOOTH
                    ), newBufferedImageWidth, 0, imageWidth, imageHeight, null
                )

                newBufferedImageWidth += imageWidth

            }
        }

        graphics.dispose()

        return CombinedResult(gachaResultImage, lostOperators)

    }

    data class CombinedResult(
        val image: BufferedImage,
        val lostItem: List<GachaItem>
    )

    fun getStarText(rare: Int): String = buildString {
        for (i in 0 until rare) {
            append("★")
        }
    }

    fun checkHasGachaTime(user: CometUser, time: Int): Boolean =
        (user.coin >= time || user.compareLevel(UserLevel.ADMIN)) && time <= 10000

    fun arkPictureIsUsable(): Boolean = cfg.arkDrawUseImage && pictureReady

    fun hasOperator(name: String): Boolean = GachaConstants.arkNightDefault.contains(name)
}