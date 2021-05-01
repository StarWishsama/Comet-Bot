package io.github.starwishsama.comet.objects.gacha.pool

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.pcr
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.gacha.items.GachaItem
import io.github.starwishsama.comet.objects.gacha.items.PCRCharacter
import io.github.starwishsama.comet.utils.GachaUtil
import java.util.stream.Collectors

class PCRPool(
    override val name: String = "白金寻访",
    override val tenjouCount: Int = 300,
    override val tenjouRare: Int = -1,
    override val poolItems: MutableList<out GachaItem> = pcr,
    override val description: String = ""
) : GachaPool() {

    private val R3 = 25
    private val R2 = 200
    private val R1 = 775

    override fun doDraw(time: Int): MutableList<PCRCharacter> {
        val drawResult = mutableListOf<PCRCharacter>()

        repeat(time) {
            val chance = RandomUtil.randomInt(0, R1 + R2 + R3)

            if ((it + 1) % 10 == 0) {
                drawResult.add(getGachaItem(2))
            } else {
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
        }

        return drawResult
    }

    override fun getGachaItem(rare: Int): PCRCharacter {
        val rareList = poolItems.parallelStream().filter { it.rare == rare }.collect(Collectors.toList())

        if (rareList.isEmpty()) throw NullPointerException("角色列表为空")

        return rareList[RandomUtil.randomInt(0, rareList.size)] as PCRCharacter
    }

    fun getPCRResult(user: BotUser?, time: Int): String {
        val startTime = System.currentTimeMillis()

        if (user == null) return getGachaResultAsString(doDraw(time), startTime)

        return if (GachaUtil.checkHasGachaTime(user, time)) {
            user.decreaseTime(time)
            getGachaResultAsString(doDraw(time), startTime)
        } else {
            GachaUtil.overTimeMessage
        }
    }

    private fun getGachaResultAsString(gachaResult: MutableList<PCRCharacter>, startTime: Long): String {
        val time = gachaResult.size

        return when {
            time <= 10 -> {
                buildString {
                    append("素敵な仲間が増えますよ!\n")
                    for ((name, star) in gachaResult) {
                        append(name).append(" ").append(GachaUtil.getStarText(star)).append(" ")
                    }
                }.trim()
            }
            else -> {
                val r3s =
                    gachaResult.parallelStream().filter { it.rare == 3 }.collect(Collectors.toList())

                val r3Character = buildString {
                    for (c in r3s) {
                        append(c.name).append(" ")
                    }
                }.trim()

                var firstTimeGetR3 = 0

                for (i in gachaResult.indices) {
                    if (gachaResult[i].rare == 3) {
                        firstTimeGetR3 = i
                        break
                    }
                }

                val r3Size = gachaResult.parallelStream().filter { it.rare == 3 }.count()
                val r2Size = gachaResult.parallelStream().filter { it.rare == 2 }.count()
                val r1Size = gachaResult.parallelStream().filter { it.rare == 1 }.count()

                """
                        素敵な仲間が増えますよ！ 
                        本次抽卡次数为 ${gachaResult.size}
                        ${if (firstTimeGetR3 != 0) "第${firstTimeGetR3}抽获得三星角色" else "酋长, 我们回家吧"}
                        $r3Character
                        ★★★×${r3Size} ★★×${r2Size} ★×${r1Size}
                        ${if (BotVariables.cfg.debugMode) "耗时: ${System.currentTimeMillis() - startTime}ms" else ""}
                    """.trimIndent()
            }
        }
    }
}