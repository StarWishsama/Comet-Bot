package io.github.starwishsama.comet.utils

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.PCRCharacter
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

    /**
     * 单抽
     */
    private fun arkNightTenDraw(): List<ArkNightOperator> {
        val ops: MutableList<ArkNightOperator> = ArrayList()
        for (i in 0..9) {
            ops.add(arkNightDraw())
        }
        return ops
    }

    /**
     * 十连
     */
    private fun arkNightDraw(): ArkNightOperator {
        val probability = RandomUtil.randomDouble(2, RoundingMode.HALF_DOWN)
        val rare: Int
        rare = when (probability) {
            in 0.48..0.50 -> 6
            in 0.0..0.08 -> 5
            in 0.40..0.90 -> 4
            else -> 3
        }
        return getOperator(rare)
    }

    /**
     * 抽卡方法
     */
    private fun getOperator(rare: Int): ArkNightOperator {
        val ops: List<ArkNightOperator> = BotVariables.arkNight
        val tempOps: MutableList<ArkNightOperator> = LinkedList()
        for (op in ops) {
            if (op.rare == rare) {
                tempOps.add(op)
            }
        }
        return tempOps[RandomUtil.randomInt(1, tempOps.size)]
    }

    /**
     * 明日方舟抽卡，返回图片
     */
    fun getArkDrawResultToImage(user: BotUser, time: Int): LinkedList<ArkNightOperator> {
        val result = LinkedList<ArkNightOperator>()
        if (user.commandTime >= time || user.compareLevel(UserLevel.ADMIN) && time <= 10000) {
            when (time) {
                1 -> {
                    user.decreaseTime()
                    var ark: ArkNightOperator = arkNightDraw()
                    result.add(ark)
                    return result
                }
                10 -> {
                    result.addAll(arkNightTenDraw())
                    user.decreaseTime(10)
                    return result
                }
            }
        }
        return result
    }

    /**
     * 根据抽卡结果合成图片
     */
    fun getArkImage(list: List<ArkNightOperator>): BufferedImage {
        val zoom:Int = 2
        //缩小图片大小，减少流量消耗
        val newBufferedImage: BufferedImage = if (list.size == 1){
            BufferedImage(256/zoom, 728/zoom, BufferedImage.TYPE_INT_RGB)
        }else{
            BufferedImage(2560/zoom, 728/zoom, BufferedImage.TYPE_INT_RGB)
        }

        val createGraphics = newBufferedImage.createGraphics()

        var newBufferedImageWidth = 0
        var newBufferedImageHeight = 0

        for ((index, i) in list.withIndex()) {
            val file = File(FileUtil.getChildFolder("res${File.separator}" + i.rare), i.name + ".jpg")
            val inStream: InputStream = file.inputStream()

            val bufferedImage: BufferedImage = ImageIO.read(inStream)

            val imageWidth = bufferedImage.width/zoom
            val imageHeight = bufferedImage.height/zoom

            createGraphics.drawImage(bufferedImage.getScaledInstance(imageWidth,imageHeight,java.awt.Image.SCALE_SMOOTH), newBufferedImageWidth, newBufferedImageHeight, imageWidth, imageHeight, null)

            newBufferedImageWidth += imageWidth

        }

        createGraphics.dispose()

        return newBufferedImage

    }
    /**
     * 明日方舟抽卡，返回文字
     */
    fun getArkDrawResult(user: BotUser, time: Int): String {
        val overTimeMessage = "今日命令条数已达上限, 请等待条数自动恢复哦~\n" +
                "命令条数现在每小时会恢复100次, 封顶1000次"
        val result = LinkedList<ArkNightOperator>()
        var r6Time = 0

        if (user.commandTime >= time || user.compareLevel(UserLevel.ADMIN) && time <= 10000) {
            when (time) {
                1 -> {
                    user.decreaseTime()
                    val (name, _, rare) = arkNightDraw()
                    return name + " " + getStar(rare)
                }
                10 -> {
                    result.addAll(arkNightTenDraw())
                    user.decreaseTime(10)
                    val sb = StringBuilder("十连结果:\n")
                    for ((name, _, rare) in result) {
                        sb.append(name).append(" ").append(getStar(rare)).append(" ")
                    }
                    return sb.toString().trim()
                }
                else -> {
                    for (i in 0 until time) {
                        if (user.commandTime >= 1 || user.compareLevel(UserLevel.ADMIN)) {
//                            user.decreaseTime(1)
                            if (i == 50) {
                                r6Time = RandomUtil.randomInt(51, time - 1)
                            }

                            if (r6Time != 0 && i == r6Time) {
                                result.add(getOperator(6))
                            } else {
                                result.add(arkNightDraw())
                            }
                        } else {
                            break
                        }
                    }
                    val r6Char = result.parallelStream().filter { it.rare == 6 }.collect(Collectors.toList())
                    val r6Text = StringBuilder()
                    r6Char.forEach { r6Text.append("${it.name} ${getStar(it.rare)} ") }

                    return "抽卡结果:\n" +
                            "抽卡次数: ${result.size}\n" +
                            "六星: ${r6Text.toString().trim()}\n" +
                            "五星个数: ${result.stream().filter { it.rare == 5 }.count()}\n" +
                            "四星个数: ${result.stream().filter { it.rare == 4 }.count()}\n" +
                            "三星个数: ${result.stream().filter { it.rare == 3 }.count()}"
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

    private fun drawPCR(): PCRCharacter {
        val chance = RandomUtil.randomInt(0, R1 + R2 + R3)
        return when {
            chance <= R3 -> {
                getCharacter(3)
            }
            chance <= R2 + R3 -> {
                getCharacter(2)
            }
            else -> {
                getCharacter(1)
            }
        }
    }

    private fun tenTimesDrawPCR(): List<PCRCharacter> {
        val result: MutableList<PCRCharacter> = LinkedList()
        for (i in 0..9) {
            result.add(drawPCR())
        }
        for (i in result.indices) {
            if (result[i].star > 2) {
                break
            } else if (i == result.size - 1 && result[i].star < 2) {
                result[i] = getCharacter(2)
            }
        }
        return result
    }

    private fun getCharacter(rare: Int): PCRCharacter {
        val temp: MutableList<PCRCharacter> = LinkedList()
        for (c in BotVariables.pcr) {
            if (c.star == rare) {
                temp.add(c)
            }
        }
        return temp[RandomUtil.randomInt(0, 1.coerceAtLeast(temp.size))]
    }

    fun getPCRResult(user: BotUser, time: Int): String {
        val reachMax = "今日抽卡次数已达上限, 别抽卡上头了"
        return if (time == 10) {
            if (user.commandTime >= 10) {
                user.decreaseTime(10)
                val ops: List<PCRCharacter> = tenTimesDrawPCR()
                val sb = java.lang.StringBuilder("十连结果:\n")
                for ((name, star) in ops) {
                    sb.append(name).append(" ").append(getStar(star)).append(" ")
                }
                sb.toString().trim { it <= ' ' }
            } else {
                reachMax
            }
        } else if (time == 1) {
            if (user.commandTime >= 1) {
                user.decreaseTime()
                val (name, star) = drawPCR()
                name + " " + getStar(star)
            } else {
                reachMax
            }
        } else {
            if (user.commandTime >= time) {
                val startTime = System.currentTimeMillis()
                val ops: MutableList<PCRCharacter> = LinkedList()

                for (i in 0 until time) {
                    if (user.commandTime > 0) {
                        user.decreaseTime()
                        if (i % 10 == 0) {
                            ops.add(getCharacter(2))
                        } else {
                            ops.add(drawPCR())
                        }
                    } else {
                        break
                    }
                }

                val r3s = ops.parallelStream().filter { (_, star) -> star == 3 }.collect(Collectors.toList())

                val sb = StringBuilder()
                for ((name) in r3s) {
                    sb.append(name).append(" ")
                }

                return """
            抽卡次数: ${ops.size}
            三星角色: ${if (sb.toString().trim { it <= ' ' }.isEmpty()) "未抽到" else sb.toString().trim { it <= ' ' }}
            二星角色数: ${ops.stream().filter { (_, star) -> star == 2 }.count()}
            一星角色数: ${ops.stream().filter { (_, star) -> star == 1 }.count()}
            耗时: ${System.currentTimeMillis() - startTime}ms
            """.trimIndent()
            } else {
                return "你要抽卡的次数大于你的抽卡次数"
            }
        }
    }

    private fun getStar(rare: Int): String {
        val sb = StringBuilder("★")
        for (i in 1 until rare) {
            sb.append("★")
        }
        return sb.toString()
    }
}