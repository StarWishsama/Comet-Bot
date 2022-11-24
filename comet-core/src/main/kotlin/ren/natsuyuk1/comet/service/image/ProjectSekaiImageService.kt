package ren.natsuyuk1.comet.service.image

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.getSurroundingRank
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiProfileEventInfo
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.math.NumberUtil.toInstant
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import ren.natsuyuk1.comet.utils.skiko.addTextln
import ren.natsuyuk1.comet.utils.skiko.changeStyle
import ren.natsuyuk1.comet.utils.string.StringUtil.toFriendly
import java.awt.Color
import java.io.File
import java.nio.file.Files
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.hours

object ProjectSekaiImageService {
    private const val WIDTH = 650
    private const val DEFAULT_PADDING = 20
    private const val AVATAR_SIZE = 80

    fun drawB30(user: ProjectSekaiUserInfo.UserGameData, b30: List<ProjectSekaiUserInfo.MusicResult>): File {
        val b30Text = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 20f)
            },
            FontUtil.fonts
        ).apply {
            addTextln("${user.userGameData.name} - ${user.userGameData.userID} - BEST 30")

            changeStyle(FontUtil.defaultFontStyle(Color.BLACK, 18f))

            addTextln()

            b30.forEach { mr ->
                val status = if (mr.isAllPerfect) "AP" else "FC"

                addTextln(
                    "${ProjectSekaiManager.getSongName(mr.musicId)} [${mr.musicDifficulty.name.uppercase()} ${
                    ProjectSekaiManager.getSongLevel(
                        mr.musicId,
                        mr.musicDifficulty
                    )
                    }] $status (${
                    ProjectSekaiManager.getSongAdjustedLevel(
                        mr.musicId,
                        mr.musicDifficulty
                    )?.fixDisplay(1)
                    })"
                )
            }

            addTextln()

            changeStyle(FontUtil.defaultFontStyle(Color.BLACK, 13f))

            addText("由 Comet 生成 | 数据来源于 profile.pjsekai.moe")
        }.build().layout(650f)

        val surface = Surface.makeRasterN32Premul(WIDTH, (DEFAULT_PADDING + b30Text.height).toInt())

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            b30Text.paint(this, 10f, 10f)
        }

        val image = surface.makeImageSnapshot()

        val tmpFile = File(cacheDirectory, "${System.currentTimeMillis()}-pjsk-b30.png").apply {
            TaskManager.registerTaskDelayed(1.hours) {
                delete()
            }

            deleteOnExit()
        }

        runBlocking { tmpFile.touch() }

        image.encodeToData(EncodedImageFormat.PNG)?.bytes?.let {
            Files.write(tmpFile.toPath(), it)
        }

        return tmpFile
    }

    suspend fun SekaiProfileEventInfo.drawEventInfo(userData: ProjectSekaiUserData?, eventId: Int): MessageWrapper {
        if (rankings.isEmpty()) {
            return "你还没打这期活动捏".toMessageWrapper()
        }

        val now = Clock.System.now()
        val profile = this.rankings.first()
        val (ahead, behind) = profile.rank.getSurroundingRank()
        val eventInfo = ProjectSekaiData.getCurrentEventInfo() ?: return "查询失败, 活动信息未加载".toMessageWrapper()
        val eventStatus = ProjectSekaiManager.getCurrentEventStatus()

        val avatarPath = ProjectSekaiManager.getAssetBundleName(profile.userCard.cardId.toInt())
            ?.let { ProjectSekaiManager.resolveCardImage(it) }

        val avatar = avatarPath?.readBytes()?.let { Image.makeFromEncoded(it) }

        val userInfoText = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 20f)
            },
            FontUtil.fonts
        ).apply {
            addTextln(profile.name)
            addText("ID: ${profile.userId}")
        }.build().layout(650f)

        val eventInfoText = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 18f)
            },
            FontUtil.fonts
        ).apply {
            addTextln("当前活动 ${eventInfo.name}")

            if (eventStatus == SekaiEventStatus.ONGOING) {
                addTextln(
                    "离活动结束还有 ${
                    (eventInfo.aggregateTime.toInstant(true) - now)
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
                    addTextln("当前队伍为 $teamName")
                    addTextln()
                }
            }

            addTextln("分数 ${profile.score} | 排名 ${profile.rank}")
            addTextln()

            if (userData != null) {
                if (userData.lastQueryScore != 0L && userData.lastQueryPosition != 0) {
                    val scoreDiff = profile.score - userData.lastQueryScore
                    val rankDiff = userData.lastQueryPosition - profile.rank

                    if (scoreDiff != 0L) {
                        addText("↑ $scoreDiff 分 ")
                    }

                    if (rankDiff != 0) {
                        addText(
                            (if (profile.rank < userData.lastQueryPosition) "↑ 上升" else " ↓ 下降") +
                                " ${rankDiff.absoluteValue} 名"
                        )
                    }

                    addTextln()
                }

                // Refresh user pjsk score and rank
                userData.updateInfo(profile.score, profile.rank)
            }

            addTextln()

            if (ahead != 0) {
                val aheadEventStatus = runBlocking { cometClient.getSpecificRankInfo(eventId, ahead) }
                val aheadScore = aheadEventStatus.getScore()

                val aheadScoreStr = aheadScore.getBetterNumber()
                val delta = (aheadScore - profile.score).getBetterNumber()
                addTextln("上一档排名 $ahead 的分数为 $aheadScoreStr, 相差 $delta")
            }

            if (behind in 200..1000000) {
                val behindEventStatus = runBlocking { cometClient.getSpecificRankInfo(eventId, behind) }
                val behindScore = behindEventStatus.getScore()

                val targetScore = behindScore.getBetterNumber()
                val deltaScore = (profile.score - behindScore).getBetterNumber()
                addTextln("下一档排名 $behind 的分数为 $targetScore, 相差 $deltaScore")
            }

            addTextln()

            addText("数据来自 PJSK Profile | Unibot API")
        }.build().layout(650f)

        val surface =
            Surface.makeRasterN32Premul(
                WIDTH,
                (AVATAR_SIZE + DEFAULT_PADDING * 3 + eventInfoText.height).toInt()
            )

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            if (avatar != null) {
                drawImageRect(
                    avatar,
                    Rect(
                        20f,
                        20f,
                        100f,
                        100f
                    )
                ) // 80 x 80
            }

            userInfoText.paint(
                this,
                DEFAULT_PADDING / 2f + AVATAR_SIZE,
                DEFAULT_PADDING.toFloat()
            )

            eventInfoText.paint(
                this,
                DEFAULT_PADDING.toFloat(),
                (AVATAR_SIZE + DEFAULT_PADDING * 2).toFloat()
            )
        }

        val image = surface.makeImageSnapshot()

        val tmpFile = File(cacheDirectory, "${System.currentTimeMillis()}-pjsk-event-info.png").apply {
            TaskManager.registerTaskDelayed(1.hours) {
                delete()
            }

            deleteOnExit()
        }

        runBlocking { tmpFile.touch() }

        image.encodeToData(EncodedImageFormat.PNG)?.bytes?.let {
            Files.write(tmpFile.toPath(), it)
        }

        return buildMessageWrapper {
            appendElement(tmpFile.asImage())
        }
    }
}
