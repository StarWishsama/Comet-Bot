package io.github.starwishsama.comet.utils

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.items.PCRCharacter
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.math.RoundingMode
import java.util.*
import java.util.stream.Collectors
import javax.imageio.ImageIO

object DrawUtil {
    /**
     * 明日方舟
     */

    const val overTimeMessage = "你要抽卡的次数超过了你的命令条数, 可以少抽一点或者等待条数自动恢复哦~\n" +
            "命令条数现在每小时会恢复100次, 封顶1000次"

    /**
     * 明日方舟抽卡方法
     */
    private fun arkNightDraw(time: Int = 1): List<ArkNightOperator> {
        val result = mutableListOf<ArkNightOperator>()
        var r6Count = 0

        repeat(time) {
            // 在五十连之后的保底
            if (it == 50) {
                r6Count = RandomUtil.randomInt(51, time - 1)
            }

            // 保底六星
            if (r6Count != 0 && it == r6Count) {
                result.add(getOperator(6))
                return@repeat
            }

            val probability = RandomUtil.randomDouble(2, RoundingMode.HALF_DOWN)
            val rare: Int
            rare = when (probability) {
                in 0.48..0.50 -> 6
                in 0.0..0.08 -> 5
                in 0.40..0.90 -> 4
                else -> 3
            }
            result.add(getOperator(rare))
        }
        return result
    }

    /**
     * 随机抽取指定星级干员
     */
    fun getOperator(rare: Int): ArkNightOperator {
        val ops: List<ArkNightOperator> = BotVariables.arkNight
        val tempOps: MutableList<ArkNightOperator> = LinkedList()
        for (op in ops) {
            if (op.rare == rare) {
                tempOps.add(op)
            }
        }

        val index = RandomUtil.randomInt(0, tempOps.size)

        return tempOps[index]
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

    /**
     * 明日方舟抽卡，返回文字
     */
    fun getArkDrawResult(user: BotUser, time: Int = 1): List<ArkNightOperator> {
        return if (checkHasGachaTime(user, time)) {
            user.decreaseTime(time)
            arkNightDraw(time)
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
                    return "单次寻访结果\n$name ${getStar(rare)}"
                }
                10 -> {
                    return StringBuilder("十连寻访结果:\n").apply {
                        for ((name, _, rare) in drawResult) {
                            append(name).append(" ").append(getStar(rare)).append(" ")
                        }
                    }.trim().toString()
                }
                else -> {
                    val r6Char = drawResult.parallelStream().filter { it.rare == 6 }.collect(Collectors.toList())
                    val r6Text = StringBuilder().apply {
                        r6Char.forEach { append("${it.name} ${getStar(it.rare)} ") }
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
            return overTimeMessage
        }
    }


    /**
     * 公主连结
     */

    private const val R3 = 25
    private const val R2 = 200
    private const val R1 = 775

    private fun drawPCR(time: Int = 1): List<PCRCharacter> {
        val drawResult = mutableListOf<PCRCharacter>()
        val chance = RandomUtil.randomInt(0, R1 + R2 + R3)

        repeat(time) {
            if (it % 10 == 0) {
                for (i in drawResult.indices) {
                    // 十连保底
                    if ((i + 1) % 10 == 0 && drawResult[i].rare < 2) {
                        drawResult[i] = getCharacter(2)
                    }
                }
                return@repeat
            }

            when {
                chance <= R3 -> {
                    drawResult.add(getCharacter(3))
                }
                chance <= R2 + R3 -> {
                    drawResult.add(getCharacter(2))
                }
                else -> {
                    drawResult.add(getCharacter(1))
                }
            }
        }

        return drawResult
    }

    private fun getCharacter(rare: Int): PCRCharacter {
        val temp: MutableList<PCRCharacter> = LinkedList()
        for (c in BotVariables.pcr) {
            if (c.rare == rare) {
                temp.add(c)
            }
        }
        return temp[RandomUtil.randomInt(0, 1.coerceAtLeast(temp.size))]
    }

    fun getPCRResult(user: BotUser, time: Int): String {
        val startTime = System.currentTimeMillis()

        if (checkHasGachaTime(user, time)) {
            user.decreaseTime(time)
            val gachaResult = drawPCR(time)

            return when {
                time <= 10 -> {
                    StringBuilder("素敵な仲間が増えますよ!\n").apply {
                        for ((name, star) in gachaResult) {
                            append(name).append(" ").append(getStar(star)).append(" ")
                        }
                    }.toString().trim()
                }
                else -> {
                    val r3s =
                        gachaResult.parallelStream().filter { (_, star) -> star == 3 }.collect(Collectors.toList())

                    val r3Character = StringBuilder().apply {
                        for ((name) in r3s) {
                            append(name).append(" ")
                        }
                    }.trim().toString()

                    var firstTimeGetR3 = 0

                    for (i in gachaResult.indices) {
                        if (gachaResult[i].rare == 3) {
                            firstTimeGetR3 = i
                            break
                        }
                    }

                    """
                        素敵な仲間が増えますよ！ 
                        本次抽卡次数为 ${gachaResult.size}
                        ${if (firstTimeGetR3 != 0) "第${firstTimeGetR3}抽获得三星角色" else "酋长, 我们回家吧"}
                        $r3Character
                        ★★★×${
                        gachaResult.parallelStream().filter { (_, star) -> star == 3 }.count()
                    } ★★×${
                        gachaResult.parallelStream().filter { (_, star) -> star == 2 }.count()
                    } ★×${gachaResult.parallelStream().filter { (_, star) -> star == 1 }.count()}
                        ${if (BotVariables.cfg.debugMode) "耗时: ${System.currentTimeMillis() - startTime}ms" else ""}
                    """.trimIndent()
                }
            }
        } else {
            return overTimeMessage
        }
    }

    fun getStar(rare: Int): String = StringBuilder().apply {
        for (i in 0 until rare) {
            append("★")
        }
    }.toString()

    fun checkHasGachaTime(user: BotUser, time: Int): Boolean =
            user.commandTime >= time || user.compareLevel(UserLevel.ADMIN) && time <= 10000
}