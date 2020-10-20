package io.github.starwishsama.comet.objects.draw.pool

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.GachaItem
import io.github.starwishsama.comet.objects.draw.items.PCRCharacter
import io.github.starwishsama.comet.utils.DrawUtil
import java.util.*
import java.util.stream.Collectors

open class PCRPool(override val name: String = "白金寻访",
                   override val tenjouCount: Int = 300,
                   override val tenjouRare: Int = -1,
                   override val poolItems: MutableList<out GachaItem> = BotVariables.pcr) : GachaPool() {

    private val R3 = 25
    private val R2 = 200
    private val R1 = 775

    override fun doDraw(time: Int): List<PCRCharacter> {
        val drawResult = mutableListOf<PCRCharacter>()
        val chance = RandomUtil.randomInt(0, R1 + R2 + R3)

        repeat(time) {
            if (it % 10 == 0) {
                for (i in drawResult.indices) {
                    // 十连保底
                    if ((i + 1) % 10 == 0 && drawResult[i].rare < 2) {
                        drawResult[i] = getGachaItem(2)
                    }
                }
                return@repeat
            }

            when {
                chance <= R3 -> {
                    drawResult.add(getGachaItem(3))
                }
                chance <= R2 + R3 -> {
                    drawResult.add(getGachaItem(2))
                }
                else -> {
                    drawResult.add(getGachaItem(1))
                }
            }
        }

        return drawResult
    }

    override fun getGachaItem(rare: Int): PCRCharacter {
        val temp: MutableList<PCRCharacter> = LinkedList()
        for (c in BotVariables.pcr) {
            if (c.rare == rare) {
                temp.add(c)
            }
        }

        if (temp.size == 0) throw NullPointerException("角色列表为空")

        return temp[RandomUtil.randomInt(0, temp.size.coerceAtLeast(1))]
    }

    open fun getPCRResult(user: BotUser, time: Int): String {
        val startTime = System.currentTimeMillis()

        if (DrawUtil.checkHasGachaTime(user, time)) {
            user.decreaseTime(time)
            val gachaResult = doDraw(time)

            return when {
                time <= 10 -> {
                    StringBuilder("素敵な仲間が増えますよ!\n").apply {
                        for ((name, star) in gachaResult) {
                            append(name).append(" ").append(DrawUtil.getStar(star)).append(" ")
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
            return DrawUtil.overTimeMessage
        }
    }
}