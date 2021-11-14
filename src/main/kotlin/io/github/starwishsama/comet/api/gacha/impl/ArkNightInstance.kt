/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.gacha.impl

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.gacha.GachaInstance
import io.github.starwishsama.comet.objects.gacha.GachaResult
import io.github.starwishsama.comet.objects.gacha.custom.CustomPool
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.objects.gacha.pool.GachaPool
import io.github.starwishsama.comet.utils.GachaUtil
import io.github.starwishsama.comet.utils.math.MathUtil

object ArkNightInstance : GachaInstance("明日方舟") {
    override fun isUsable() {
        TODO("Not yet implemented")
    }

    override fun checkUpdate(): Boolean {
        TODO("Not yet implemented")
    }

    override fun downloadFile() {
        TODO("Not yet implemented")
    }

    override fun parseGachaResult(result: GachaResult): String {
        TODO("Not yet implemented")
    }

    override fun parseCustomPool(customPool: CustomPool): GachaPool {
        val pool = ArkNightPool(
            customPool.poolName,
            customPool.displayPoolName,
            customPool.poolDescription
        ) {
            (GachaUtil.hasOperator(this.name) || customPool.modifiedGachaItems.stream().filter { it.name == this.name }
                .findAny().isPresent) &&
                    (if (customPool.condition.isNotEmpty()) !customPool.condition.contains(obtain) else true)
        }

        customPool.modifiedGachaItems.forEach { item ->
            val result = pool.poolItems.stream().filter { it.name == item.name }.findAny()

            result.ifPresent {
                if (item.isHidden) {
                    pool.poolItems.remove(it)
                    return@ifPresent
                }

                if (item.probability > 0) {
                    if (item.weight <= 1) {
                        pool.highProbabilityItems[it] = item.probability
                    } else {
                        pool.highProbabilityItems[it] = MathUtil.calculateWeight(
                            pool.poolItems.size,
                            pool.poolItems.filter { poolItem -> poolItem.rare == result.get().rare }.size,
                            item.weight
                        )
                    }
                }
            }.also {
                if (!result.isPresent) {
                    CometVariables.daemonLogger.warning("名为 ${item.name} 的抽卡物品不存在于游戏数据中")
                }
            }
        }

        return pool
    }
}