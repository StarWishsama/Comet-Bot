/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai

import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiMusicInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiProfileEventInfo
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiI18N
import ren.natsuyuk1.comet.objects.pjsk.local.PJSKProfileMusic
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusic
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusicDifficulty
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.datetime.format
import ren.natsuyuk1.comet.utils.datetime.toFriendly
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.math.NumberUtil.toInstant
import ren.natsuyuk1.comet.utils.time.yyMMddWithTimeZonePattern
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

private val rankPosition =
    listOf(
        1,
        2,
        3,
        4,
        5,
        10,
        50,
        100,
        200,
        500,
        1000,
        2000,
        3000,
        4000,
        5000,
        10000,
        20000,
        50000,
        100000
    )

internal suspend fun SekaiProfileEventInfo.toMessageWrapper(
    userData: ProjectSekaiUserData?,
    eventId: Int
): MessageWrapper {
    if (rankings.isEmpty()) {
        return "你还没打这期活动捏".toMessageWrapper()
    }

    val now = Clock.System.now()
    val profile = rankings.first()
    val (ahead, behind) = profile.rank.getSurroundingRank()
    val eventInfo = ProjectSekaiData.getCurrentEventInfo() ?: return "查询失败, 活动信息未加载".toMessageWrapper()
    val eventStatus = ProjectSekaiManager.getCurrentEventStatus()

    return buildMessageWrapper {
        appendTextln("${profile.name} - ${profile.userId}")
        appendLine()
        appendTextln("当前活动 ${eventInfo.name}")
        if (eventStatus == SekaiEventStatus.ONGOING) {
            appendTextln(
                "离活动结束还有 ${
                (eventInfo.aggregateTime.toInstant(true) - now)
                    .toFriendly(TimeUnit.SECONDS)
                }"
            )
        }

        if (profile.userCheerfulCarnival.cheerfulCarnivalTeamId != null) {
            val teamName =
                ProjectSekaiI18N.getCarnivalTeamName(profile.userCheerfulCarnival.cheerfulCarnivalTeamId)

            if (teamName != null) {
                appendTextln("当前队伍为 $teamName")
                appendLine()
            }
        }

        appendTextln("分数 ${profile.score} | 排名 ${profile.rank}")
        appendLine()

        if (userData != null) {
            if (userData.lastQueryScore != 0L && userData.lastQueryPosition != 0) {
                val scoreDiff = profile.score - userData.lastQueryScore
                val rankDiff = userData.lastQueryPosition - profile.rank

                if (scoreDiff != 0L) {
                    appendText("+ $scoreDiff 分")
                }

                if (rankDiff != 0) {
                    appendText(
                        (if (profile.rank < userData.lastQueryPosition) " ↑ 上升" else " ↓ 下降") +
                            " ${rankDiff.absoluteValue} 名"
                    )
                }

                appendLine()
            }

            // Refresh user pjsk score and rank
            userData.updateInfo(profile.score, profile.rank)
        }

        appendLine()

        if (ahead != 0) {
            val aheadEventStatus = cometClient.getSpecificRankInfo(eventId, ahead)
            val aheadScore = aheadEventStatus.getScore()

            if (aheadScore != -1L) {
                val aheadScoreStr = aheadScore.getBetterNumber()
                val delta = (aheadScore - profile.score).getBetterNumber()
                appendTextln("上一档排名 $ahead 的分数为 $aheadScoreStr, 相差 $delta")
            } else {
                appendTextln("上一档排名 $ahead 暂无数据")
            }
        }

        if (behind in 200..100000) {
            val behindEventStatus = cometClient.getSpecificRankInfo(eventId, behind)
            val behindScore = behindEventStatus.getScore()

            if (behindScore != -1L) {
                val targetScore = behindScore.getBetterNumber()
                val deltaScore = (profile.score - behindScore).getBetterNumber()
                appendTextln("下一档排名 $behind 的分数为 $targetScore, 相差 $deltaScore")
            } else {
                appendTextln("下一档排名 $behind 暂无数据")
            }
        }

        appendLine()

        appendText("数据来自 PJSK Profile | Unibot API | 33Kit")
    }
}

internal fun Int.getSurroundingRank(): Pair<Int, Int> {
    if (this <= rankPosition.first()) {
        return Pair(0, rankPosition.first())
    }

    var before: Int
    var after: Int

    for (i in rankPosition.indices) {
        if (i == rankPosition.size - 1) {
            break
        }

        before = rankPosition[i]
        after = rankPosition[i + 1]

        if (this in before + 1..after) {
            if (before == this && i != 0) {
                before = rankPosition[i - 1]
            } else if (after == this && i + 1 != rankPosition.size - 1) {
                after = rankPosition[i + 2]
            }

            return Pair(before, after)
        }
    }

    return Pair(rankPosition.last(), 1000001)
}

internal suspend fun ProjectSekaiMusicInfo.toMessageWrapper() =
    buildMessageWrapper {
        val musicInfo = this@toMessageWrapper
        val diff = ProjectSekaiMusicDifficulty.getMusicDifficulty(musicInfo.id)
        val extraInfo = PJSKProfileMusic.getMusicInfo(musicInfo.id)

        if (diff.size != 5) {
            appendText("音乐等级数据还没有准备好哦")
            return@buildMessageWrapper
        }

        if (musicInfo.publishedAt.toInstant(true) > Clock.System.now()) {
            appendTextln("⚠ 该内容为未公开剧透内容")
        }

        appendElement(ProjectSekaiMusic.getMusicCover(musicInfo).asImage())
        appendLine()
        appendTextln(musicInfo.title)
        appendLine()
        appendTextln("作词 ${musicInfo.lyricist}")
        appendTextln("作曲 ${musicInfo.composer}")
        appendTextln("编曲 ${musicInfo.arranger}")
        if (extraInfo != null) {
            appendTextln("时长 ${extraInfo.duration.seconds.toFriendly()}")
        }
        appendTextln("上线时间 ${musicInfo.publishedAt.toInstant(true).format(yyMMddWithTimeZonePattern)}")
        appendLine()

        appendTextln("难度信息 >")
        diff.forEach {
            appendTextln(
                "${it.musicDifficulty}[${it.playLevel}] | ${it.totalNoteCount}"
            )
        }

        trim()
    }
