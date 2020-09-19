package io.github.starwishsama.comet.objects.draw.pool

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
import io.github.starwishsama.comet.utils.DrawUtil
import io.github.starwishsama.comet.utils.FileUtil
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.math.RoundingMode
import java.util.*
import java.util.stream.Collectors
import javax.imageio.ImageIO

class ArkNightPool : GachaPool() {
    override val name: String = "ArkNight"
    override val tenjouCount: Int = -1
    override val tenjouRare: Int = -1
    override val poolItems: MutableList<ArkNightOperator> = BotVariables.arkNight

    override fun doDraw(time: Int): List<ArkNightOperator> {
        val result = mutableListOf<ArkNightOperator>()
        var r6Count = 0

        repeat(time) {
            // 在五十连之后的保底
            if (it == 50) {
                r6Count = RandomUtil.randomInt(51, time - 1)
            }

            // 首先检查是否到了出保底六星的时候
            if (r6Count != 0 && it == r6Count) {
                result.add(getGachaItem(6))
                return@repeat
            }

            val probability = RandomUtil.randomDouble(2, RoundingMode.HALF_DOWN)

            // 然后检查是否到了出 UP 角色的时候
            if (highProbabilityItems.isNotEmpty()) {
                highProbabilityItems.forEach { (gachaItem, range) ->
                    if (probability in range) {
                        result.add(gachaItem as ArkNightOperator)
                        return@repeat
                    }
                }
            }

            val rare: Int
            rare = when (probability) {
                in 0.48..0.50 -> 6
                in 0.0..0.08 -> 5
                in 0.40..0.90 -> 4
                else -> 3
            }
            result.add(getGachaItem(rare))
        }
        return result
    }

    override fun getGachaItem(rare: Int): ArkNightOperator {
        val rareItems: MutableList<ArkNightOperator> = LinkedList()
        val operators = poolItems.parallelStream().filter { it.rare == rare }.collect(Collectors.toList())

        rareItems.addAll(operators)

        val index = RandomUtil.randomInt(0, rareItems.size)

        return rareItems[index]
    }

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
            val file = File(FileUtil.getChildFolder(FileUtil.getResourceFolder().toString() + File.separator + i.rare), i.name + ".jpg")
            // 判断对应图片文件是否存在
            if (file.exists()) {
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

        }

        createGraphics.dispose()

        return newBufferedImage

    }

    /**
     * 明日方舟抽卡结果
     */
    fun getArkDrawResult(user: BotUser, time: Int = 1): List<ArkNightOperator> {
        return if (DrawUtil.checkHasGachaTime(user, time)) {
            user.decreaseTime(time)
            doDraw(time)
        } else {
            emptyList()
        }
    }

    /**
     * 明日方舟抽卡，返回文字
     */
    fun getArkDrawResultAsString(user: BotUser, time: Int): String {
        val drawResult = getArkDrawResult(user, time)
        if (drawResult.isNotEmpty()) {
            when (time) {
                1 -> {
                    val (name, _, rare) = drawResult[0]
                    return "单次寻访结果\n$name ${DrawUtil.getStar(rare)}"
                }
                10 -> {
                    return StringBuilder("十连寻访结果:\n").apply {
                        for ((name, _, rare) in drawResult) {
                            append(name).append(" ").append(DrawUtil.getStar(rare)).append(" ")
                        }
                    }.trim().toString()
                }
                else -> {
                    val r6Char = drawResult.parallelStream().filter { it.rare == 6 }.collect(Collectors.toList())
                    val r6Text = StringBuilder().apply {
                        r6Char.forEach { append("${it.name} ${DrawUtil.getStar(it.rare)} ") }
                    }.toString().trim()

                    return "寻访结果:\n" +
                            "寻访次数: ${drawResult.size}\n" +
                            "六星: ${r6Text}\n" +
                            "五星个数: ${drawResult.parallelStream().filter { it.rare == 5 }.count()}\n" +
                            "四星个数: ${drawResult.parallelStream().filter { it.rare == 4 }.count()}\n" +
                            "三星个数: ${drawResult.parallelStream().filter { it.rare == 3 }.count()}"
                }
            }
        } else {
            return DrawUtil.overTimeMessage
        }
    }
}