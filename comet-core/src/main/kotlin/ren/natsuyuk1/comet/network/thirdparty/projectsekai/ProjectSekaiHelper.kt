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
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiMusicInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiI18N
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

internal fun List<ProjectSekaiEventInfo>.toMessageWrapper(
    userData: ProjectSekaiUserData?,
    position: Int,
): MessageWrapper {
    val top100 = this
    val now = Clock.System.now()
    val index = top100.indexOfFirst { it.userId == userData?.userID || it.rank == position }
    val profile = top100[index]
    val eventInfo = ProjectSekaiData.getCurrentEventInfo() ?: return "查询失败, 活动信息未加载".toMessageWrapper()
    val eventStatus = ProjectSekaiManager.getCurrentEventStatus()

    var aheadEventStatus: ProjectSekaiEventInfo? = null
    var behindEventStatus: ProjectSekaiEventInfo? = null

    if (index > 0) {
        aheadEventStatus = top100[index - 1]
    }

    if (index < 100) {
        behindEventStatus = top100[index + 1]
    }

    return buildMessageWrapper {
        appendTextln("${profile.name} - ${profile.userId}")
        appendLine()
        appendTextln("当前活动 ${eventInfo.name}")
        if (eventStatus == SekaiEventStatus.ONGOING) {
            appendTextln(
                "离活动结束还有 ${
                (eventInfo.aggregateTime.toInstant(true) - now)
                    .toFriendly(TimeUnit.SECONDS)
                }",
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
                            " ${rankDiff.absoluteValue} 名",
                    )
                }

                appendLine()
            }

            // Refresh user pjsk score and rank
            userData.updateInfo(profile.score, profile.rank)
        }

        appendLine()

        if (aheadEventStatus != null) {
            val aheadScore = aheadEventStatus.score

            if (aheadScore != -1L) {
                val aheadScoreStr = aheadScore.getBetterNumber()
                val delta = (aheadScore - profile.score).getBetterNumber()
                appendTextln("上一档排名 ${aheadEventStatus.rank} 的分数为 $aheadScoreStr, 相差 $delta")
            } else {
                appendTextln("上一档排名 ${aheadEventStatus.rank} 暂无数据")
            }
        }

        if (behindEventStatus != null) {
            val behindScore = behindEventStatus.score

            if (behindScore != -1L) {
                val targetScore = behindScore.getBetterNumber()
                val deltaScore = (profile.score - behindScore).getBetterNumber()
                appendTextln("下一档排名 ${behindEventStatus.rank} 的分数为 $targetScore, 相差 $deltaScore")
            } else {
                appendTextln("下一档排名 ${behindEventStatus.rank} 暂无数据")
            }
        }

        appendLine()

        appendText("数据来自 PJSK Profile | Unibot API")
    }
}

internal suspend fun ProjectSekaiMusicInfo.toMessageWrapper() =
    buildMessageWrapper {
        val musicInfo = this@toMessageWrapper

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
        appendTextln("上线时间 ${musicInfo.publishedAt.toInstant(true).format(yyMMddWithTimeZonePattern)}")
        appendLine()

        appendTextln("难度信息 >")
        MusicDifficulty.values().forEach {
            val diff = ProjectSekaiMusicDifficulty.getDifficulty(id, it) ?: return@forEach
            appendTextln("$it[${diff.playLevel}] | ${diff.totalNoteCount}")
        }

        trim()
    }
