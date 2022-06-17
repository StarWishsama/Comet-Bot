/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai

import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.consts.client
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiProfile
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.SekaiBestPredictionInfo
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper

private val rankPosition = listOf(100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000)

fun ProjectSekaiProfile.toMessageWrapper(): MessageWrapper {
    val profile = this@toMessageWrapper.rankings[0]
    val rank = profile.rank

    return buildMessageWrapper {
        appendText("${profile.name} - ${profile.userId}", true)
        appendLine()
        appendText("分数 ${profile.score} | 排名 $rank", true)
        appendLine()

    }
}

private fun ULong.getSurroundingRank(): Pair<Int, Int> {
    for (i in rankPosition.indices) {
        if (i == rankPosition.size - 1) {
            break
        }

        val before = rankPosition[i]
        val after = rankPosition[i + 1]

        if (this > before.toUInt() && this <= after.toUInt()) {
            return Pair(before, after)
        }
    }

    return Pair(rankPosition.last(), 1000001)
}

object ProjectSekaiHelper {
    lateinit var predictionCache: SekaiBestPredictionInfo
    private val scope = ModuleScope("projectsekai_helper")

    fun refreshCache() {
        scope.launch {
            predictionCache = client.getRankPredictionInfo()
        }
    }
}

