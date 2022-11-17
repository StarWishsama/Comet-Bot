/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiProfileEventInfo
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.math.NumberUtil.toInstant
import ren.natsuyuk1.comet.utils.string.StringUtil.toFriendly

private val rankPosition = listOf(100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000)

fun SekaiProfileEventInfo.toMessageWrapper(userData: ProjectSekaiUserData?, eventId: Int): MessageWrapper {
    if (rankings.isEmpty()) {
        return "你还没打这期活动捏".toMessageWrapper()
    }

    val now = Clock.System.now()
    val profile = this@toMessageWrapper.rankings.first()
    val (ahead, behind) = profile.rank.getSurroundingRank()

    return buildMessageWrapper {
        appendTextln("${profile.name} - ${profile.userId}")
        appendLine()
        appendTextln("当前活动 ${ProjectSekaiData.getCurrentEventInfo()?.name}")
        if (ProjectSekaiData.getCurrentEventInfo()?.aggregateTime != null) {
            appendTextln(
                "离活动结束还有 ${
                (ProjectSekaiData.getCurrentEventInfo()?.aggregateTime!!.toInstant(true) - now)
                    .toFriendly(
                        msMode = false
                    )
                }"
            )
        }
        if (profile.userCheerfulCarnival.cheerfulCarnivalTeamId != null) {
            val teamName =
                ProjectSekaiManager.getCarnivalTeamI18nName(profile.userCheerfulCarnival.cheerfulCarnivalTeamId)

            if (teamName != null) {
                appendTextln("当前队伍为 $teamName")
            }
        }
        appendLine()
        appendTextln("分数 ${profile.score} | 排名 ${profile.rank}")
        appendLine()

        if (userData != null) {
            if (userData.lastQueryScore != 0L && userData.lastQueryPosition != 0) {
                val scoreDiff = getDifference(userData.lastQueryScore, profile.score)
                val rankDiff = getDifference(userData.lastQueryPosition.toLong(), profile.rank.toLong())

                if (scoreDiff != 0L) {
                    appendText("↑ 上升 $scoreDiff 分")
                }

                if (rankDiff != 0L) {
                    appendText((if (profile.rank < userData.lastQueryPosition) " ↑ 上升" else " ↓ 下降") + " $rankDiff 名")
                }

                appendLine()
            }

            // Refresh user pjsk score and rank
            userData.updateInfo(profile.score, profile.rank)
        }

        if (ahead != 0) {
            val aheadEventStatus = runBlocking { cometClient.getSpecificRankInfo(eventId, ahead) }
            val aheadScore = aheadEventStatus.getScore()

            val aheadScoreStr = aheadScore.getBetterNumber()
            val delta = (aheadScore - profile.score).getBetterNumber()
            appendTextln("上一档排名 $ahead 的分数为 $aheadScoreStr, 相差 $delta")
        }

        if (behind in 100..1000001) {
            val behindEventStatus = runBlocking { cometClient.getSpecificRankInfo(eventId, behind) }
            val behindScore = behindEventStatus.getScore()

            val targetScore = behindScore.getBetterNumber()
            val deltaScore = (profile.score - behindScore).getBetterNumber()
            appendTextln("下一档排名 $behind 的分数为 $targetScore, 相差 $deltaScore")
        }

        appendLine()

        appendText("数据来自 Project Sekai Profile | 33Kit")
    }
}

private fun getDifference(before: Long, now: Long): Long =
    if (before > now) {
        before - now
    } else {
        now - before
    }

private fun Int.getSurroundingRank(): Pair<Int, Int> {
    if (this < rankPosition.first()) {
        return Pair(0, rankPosition.first())
    }

    for (i in rankPosition.indices) {
        if (i == rankPosition.size - 1) {
            break
        }

        val before = rankPosition[i]
        val after = rankPosition[i + 1]

        if (this in (before + 1)..after) {
            return Pair(before, after)
        }
    }

    return Pair(rankPosition.last(), 1000001)
}
