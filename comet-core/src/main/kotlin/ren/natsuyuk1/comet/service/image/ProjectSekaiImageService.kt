package ren.natsuyuk1.comet.service.image

import kotlinx.datetime.Clock
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.getSurroundingRank
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiProfileEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official.PJSKMusicInfo
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiCard
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiCharts
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiI18N
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusic
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.datetime.toFriendly
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.math.NumberUtil.toInstant
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import ren.natsuyuk1.comet.utils.skiko.addTextln
import ren.natsuyuk1.comet.utils.skiko.changeStyle
import java.awt.Color
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

object ProjectSekaiImageService {
    private const val WIDTH = 550
    private const val DEFAULT_PADDING = 20
    private const val AVATAR_SIZE = 100
    private val BETTER_GRAY_RGB: Int by lazy { Color(220, 220, 220).rgb }

    fun drawB30(user: ProjectSekaiUserInfo.UserGameData, b30: List<ProjectSekaiUserInfo.MusicResult>): MessageWrapper {
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
                    "${ProjectSekaiMusic.getMusicInfo(mr.musicId)?.title} [${mr.musicDifficulty.name.uppercase()} ${
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
        }.build().layout(WIDTH.toFloat())

        val surface = Surface.makeRasterN32Premul(WIDTH, (DEFAULT_PADDING + b30Text.height).toInt())

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            b30Text.paint(this, 10f, 10f)
        }

        val image = surface.makeImageSnapshot()
        val data = image.encodeToData(EncodedImageFormat.PNG)
            ?: return buildMessageWrapper {
                appendText("生成图片失败!")
            }

        return buildMessageWrapper {
            appendElement(data.bytes.inputStream().asImage())
        }
    }

    suspend fun SekaiProfileEventInfo.drawEventInfo(
        eventId: Int,
        userData: ProjectSekaiUserData? = null
    ): MessageWrapper {
        if (rankings.isEmpty()) {
            return "你还没打这期活动捏".toMessageWrapper()
        }

        val now = Clock.System.now()
        val profile = this.rankings.first()
        val (ahead, behind) = profile.rank.getSurroundingRank()
        val eventInfo = ProjectSekaiData.getCurrentEventInfo() ?: return "查询失败, 活动信息未加载".toMessageWrapper()
        val eventStatus = ProjectSekaiManager.getCurrentEventStatus()

        val avatarBundleName = ProjectSekaiCard.getAssetBundleName(profile.userCard.cardId.toInt())

        var avatarFile: File? = null
        var avatar: Image? = null

        if (avatarBundleName != null) {
            avatarFile = ProjectSekaiManager.resolveCardImage(avatarBundleName)
        }

        if (avatarFile?.exists() == true && avatarFile.length() != 0L) {
            avatar = Image.makeFromEncoded(avatarFile.readBytes())
        }

        val userInfoText = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 20f)
            },
            FontUtil.fonts
        ).apply {
            addTextln(profile.name)
            addText("ID: ${profile.userId}")
        }.build().layout(WIDTH.toFloat())

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
                        .toFriendly(TimeUnit.SECONDS)
                    }"
                )
            }

            if (profile.userCheerfulCarnival.cheerfulCarnivalTeamId != null) {
                val teamName =
                    ProjectSekaiI18N.getCarnivalTeamName(profile.userCheerfulCarnival.cheerfulCarnivalTeamId)

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
                val aheadEventStatus = cometClient.getSpecificRankInfo(eventId, ahead)
                val aheadScore = aheadEventStatus.getScore()

                if (aheadScore != -1L) {
                    val aheadScoreStr = aheadScore.getBetterNumber()
                    val delta = (aheadScore - profile.score).getBetterNumber()
                    addTextln("上一档排名 $ahead 的分数为 $aheadScoreStr, 相差 $delta")
                } else {
                    addTextln("上一档排名 $ahead 暂无数据")
                }
            }

            if (behind in 200..1000000) {
                val behindEventStatus = cometClient.getSpecificRankInfo(eventId, behind)
                val behindScore = behindEventStatus.getScore()

                if (behindScore != -1L) {
                    val targetScore = behindScore.getBetterNumber()
                    val deltaScore = (profile.score - behindScore).getBetterNumber()
                    addTextln("下一档排名 $behind 的分数为 $targetScore, 相差 $deltaScore")
                } else {
                    addTextln("下一档排名 $behind 暂无数据")
                }
            }

            addTextln()

            addText("数据来自 PJSK Profile | Unibot API")
        }.build().layout(WIDTH.toFloat())

        val surface =
            Surface.makeRasterN32Premul(
                WIDTH,
                (AVATAR_SIZE + DEFAULT_PADDING * 3 + eventInfoText.height).toInt()
            )

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            if (avatar != null) {
                val rrect = RRect.makeXYWH(
                    20f,
                    20f,
                    AVATAR_SIZE.toFloat(),
                    AVATAR_SIZE.toFloat(),
                    10f // 圆形弧度
                )

                save()
                clipRRect(rrect, true)
                drawImageRect(
                    avatar,
                    Rect(0f, 0f, avatar.width.toFloat(), avatar.height.toFloat()),
                    rrect,
                    FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                    null,
                    true
                ) // 80 x 80
                restore()
            }

            userInfoText.paint(
                this,
                DEFAULT_PADDING * 2f + AVATAR_SIZE,
                DEFAULT_PADDING.toFloat()
            )

            eventInfoText.paint(
                this,
                DEFAULT_PADDING.toFloat(),
                (AVATAR_SIZE + DEFAULT_PADDING * 2).toFloat()
            )
        }

        val image = surface.makeImageSnapshot()
        val data = image.encodeToData(EncodedImageFormat.PNG)
            ?: return buildMessageWrapper {
                appendText("生成图片失败!")
            }

        return buildMessageWrapper {
            appendElement(data.bytes.inputStream().asImage())
        }
    }

    suspend fun drawCharts(
        musicInfo: PJSKMusicInfo,
        difficulty: MusicDifficulty
    ): MessageWrapper {
        var chartFiles = ProjectSekaiCharts.getCharts(musicInfo, difficulty)

        if (chartFiles.isEmpty()) {
            ProjectSekaiCharts.downloadChart(musicInfo)
            chartFiles = ProjectSekaiCharts.getCharts(musicInfo, difficulty)
        }

        val bg = Image.makeFromEncoded(chartFiles[0].readBytes())
        val bar = Image.makeFromEncoded(chartFiles[1].readBytes())
        val chart = Image.makeFromEncoded(chartFiles[2].readBytes())

        val text = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 70f)
            },
            FontUtil.fonts
        ).apply {
            addTextln(
                "${musicInfo.title} - ${musicInfo.lyricist}"
            )
            popStyle()
            pushStyle(FontUtil.defaultFontStyle(Color.BLACK, 38f))
            addText(
                "${difficulty.name} [Lv.${
                ProjectSekaiManager.getSongLevel(
                    musicInfo.id,
                    difficulty
                )
                }]"
            )
        }.build().layout((bg.width + DEFAULT_PADDING).toFloat())

        val rightText = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.RIGHT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 35f)
            },
            FontUtil.fonts
        ).apply {
            addTextln("谱面数据来自 プロセカ譜面保管所")
            addTextln("Render by Comet")
        }.build().layout(bg.width - 250f)

        val surface = Surface.makeRasterN32Premul(
            bg.width + DEFAULT_PADDING,
            bg.height + DEFAULT_PADDING * 3 + text.height.toInt()
        )

        val cover = Image.makeFromEncoded(ProjectSekaiMusic.getMusicCover(musicInfo).readBytes())

        surface.canvas.apply {
            clear(Color.WHITE.rgb)
            drawRect(
                Rect(10f, 10f, 10f + bg.width, 10f + bg.height),
                Paint().apply {
                    color = BETTER_GRAY_RGB // #DCDCDC
                }
            )
            drawImage(bg, 10f, 10f)
            save()
            drawImage(bar, 10f, 10f)
            restore()
            save()
            drawImage(chart, 10f, 10f)
            restore()

            val rrect = RRect.makeXYWH(
                30f,
                40f + bg.height,
                text.height,
                text.height,
                0f
            )

            save()
            clipRRect(rrect, true)
            drawImageRect(
                cover,
                Rect(0f, 0f, cover.width.toFloat(), cover.height.toFloat()),
                rrect,
                FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                null,
                true
            )
            restore()

            text.paint(this, 60f + rrect.width, 27f + bg.height)
            rightText.paint(this, 60f + rrect.width, 30f + bg.height)
        }

        val data = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)
            ?: return buildMessageWrapper {
                appendText("图片生成失败!")
            }

        return buildMessageWrapper {
            appendElement(data.bytes.inputStream().asImage())
        }
    }
}
