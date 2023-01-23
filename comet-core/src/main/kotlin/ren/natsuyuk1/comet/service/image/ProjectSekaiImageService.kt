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
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.*
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.objects.pjsk.local.*
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.datetime.toFriendly
import ren.natsuyuk1.comet.utils.file.isBlank
import ren.natsuyuk1.comet.utils.file.isType
import ren.natsuyuk1.comet.utils.file.touch
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

/**
 * 负责绘制 Project Sekai 部分功能的图片
 */
object ProjectSekaiImageService {
    /**
     * 代表绘制图片的通用宽度
     */
    private const val WIDTH = 550

    /**
     * 代表图片内容的填充大小
     */
    private const val DEFAULT_PADDING = 20

    /**
     * 代表角色卡图片的大小
     * 原图为 128x128
     */
    private const val AVATAR_SIZE = 100

    /**
     * 代表歌曲封面的大小
     */
    private const val COVER_SIZE = 50

    /**
     * 谱面背景色
     */
    private val BETTER_GRAY_RGB: Int by lazy { Color(220, 220, 220).rgb }

    /**
     * Skia 绘制质量参数, 使用较低值以压缩大小
     */
    private const val QUALITY = 90

    /**
     * 绘制一张玩家的 30 首最佳歌曲表
     *
     * @param user 从 API 获得的 [ProjectSekaiUserInfo]
     * @param b30 表现最佳的 30 首歌
     *
     * @return 渲染后图片 [Image]
     */
    suspend fun drawBest30(user: ProjectSekaiUserInfo, b30: List<ProjectSekaiUserInfo.MusicResult>): Image? {
        val avatar = user.userProfile
        TODO()
    }

    private suspend fun Canvas.drawMusicInfo(
        musicResult: ProjectSekaiUserInfo.MusicResult,
        x: Float,
        y: Float
    ) {
        val musicInfo = ProjectSekaiMusic.getMusicInfo(musicResult.musicId) ?: return
        val musicLevel = ProjectSekaiManager.getSongAdjustedLevel(
            musicResult.musicId,
            musicResult.musicDifficulty,
            musicResult.playResult
        )?.fixDisplay(1)
        val coverFile = ProjectSekaiMusic.getMusicCover(musicInfo)

        val cover = try {
            Image.makeFromEncoded(coverFile.readBytes())
        } catch (e: Exception) {
            // TODO: 使用一张占位符图片替换?
            return
        }

        val rrect = RRect.makeXYWH(
            x,
            y,
            COVER_SIZE.toFloat(),
            COVER_SIZE.toFloat(),
            10f // 圆形弧度
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

        val musicParagraph = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 15f)
            },
            FontUtil.fonts
        ).apply {
            addTextln(musicLevel ?: "N/A")

            changeStyle(FontUtil.defaultFontStyle(Color.BLACK, 20f))

            addTextln(musicInfo.title)

            changeStyle(FontUtil.defaultFontStyle(Color.BLACK, 16f))
            addText(if (musicResult.isAllPerfect) "ALL PERFECT" else "FULL COMBO")
        }.build().layout(50f)

        musicParagraph.paint(this, x + COVER_SIZE + 5f, y)
    }

    /**
     * 绘制一张玩家的 30 首最佳歌曲表
     *
     * @param user 从 API 获得的 [ProjectSekaiUserInfo]
     * @param b30 表现最佳的 30 首歌
     *
     * @return 包装后的消息 [MessageWrapper]
     */
    @Deprecated("This method will replace to better one", replaceWith = ReplaceWith("drawBest30"))
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
                        mr.musicDifficulty,
                        mr.playResult
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
        val data = image.encodeToData(EncodedImageFormat.JPEG, QUALITY)
            ?: return buildMessageWrapper {
                appendText("生成图片失败!")
            }

        return buildMessageWrapper {
            appendElement(data.bytes.asImage())
        }
    }

    /**
     * 绘制一张玩家的活动信息
     *
     * @param eventId 活动 ID
     * @param userData 用户活动积分数据 [ProjectSekaiUserData]
     *
     * @return 包装后的消息 [MessageWrapper]
     */
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

        // 获取头像内部名称
        val avatarBundleName = ProjectSekaiCard.getAssetBundleName(profile.userCard.cardId.toInt())

        var avatarFile: File? = null
        var avatar: Image? = null

        if (avatarBundleName != null) {
            avatarFile = ProjectSekaiManager.resolveCardImage(avatarBundleName, profile.userCard.specialTrainingStatus)
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

            when (eventStatus) {
                SekaiEventStatus.ONGOING -> {
                    addTextln(
                        "离活动结束还有 ${
                        (eventInfo.aggregateTime.toInstant(true) - now)
                            .toFriendly(TimeUnit.SECONDS)
                        }"
                    )
                }

                SekaiEventStatus.END -> {
                    addTextln("当前活动已结束")
                }

                else -> {}
            }
        }.build().layout(WIDTH.toFloat())

        val eventTeamText = if (profile.userCheerfulCarnival.cheerfulCarnivalTeamId != null) {
            ParagraphBuilder(
                ParagraphStyle().apply {
                    alignment = Alignment.LEFT
                    textStyle = FontUtil.defaultFontStyle(Color.BLACK, 18f)
                },
                FontUtil.fonts
            ).apply {
                val teamName =
                    ProjectSekaiI18N.getCarnivalTeamName(profile.userCheerfulCarnival.cheerfulCarnivalTeamId)

                if (teamName != null) {
                    addTextln("当前队伍为 $teamName")
                }
            }.build().layout(WIDTH.toFloat())
        } else null

        val eventScoreText = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 18f)
            },
            FontUtil.fonts
        ).apply {
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
                    addTextln()
                }

                // Refresh user pjsk score and rank
                userData.updateInfo(profile.score, profile.rank)
            }

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

            addTextln("数据来自 Unibot API / PJSK Profile")
            addText("Render by Comet")
        }.build().layout(WIDTH.toFloat())

        val surface =
            Surface.makeRasterN32Premul(
                WIDTH,
                (
                    AVATAR_SIZE +
                        DEFAULT_PADDING * 2.5 +
                        eventInfoText.height +
                        eventScoreText.height +
                        (eventTeamText?.height ?: 0f)
                    ).toInt()
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

            var extraY = 0f

            if (eventInfo.eventType == "cheerful_carnival" &&
                profile.userCheerfulCarnival.cheerfulCarnivalTeamId != null &&
                eventTeamText != null
            ) {
                val teamNum = if (profile.userCheerfulCarnival.cheerfulCarnivalTeamId % 2 == 0) 2 else 1
                val teamIcon =
                    ProjectSekaiEvent.getEventTeamImage(teamNum)
                var extraX = 0f

                if (teamIcon != null) {
                    val teamIconImg = Image.makeFromEncoded(teamIcon.readBytes())
                    val rect = Rect.makeXYWH(
                        DEFAULT_PADDING.toFloat(),
                        AVATAR_SIZE + DEFAULT_PADDING * 2 + eventInfoText.height,
                        30f,
                        30f
                    )

                    save()
                    clipRect(rect, true)
                    drawImageRect(
                        teamIconImg,
                        Rect(0f, 0f, teamIconImg.width.toFloat(), teamIconImg.height.toFloat()),
                        rect,
                        FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                        null,
                        true
                    )
                    restore()

                    extraX = rect.width
                }

                eventTeamText.paint(
                    this,
                    DEFAULT_PADDING + extraX + 10f,
                    AVATAR_SIZE + DEFAULT_PADDING * 2 + eventInfoText.height
                )

                extraY = eventTeamText.height
            }

            eventScoreText.paint(
                this,
                DEFAULT_PADDING.toFloat(),
                AVATAR_SIZE + DEFAULT_PADDING * 2 + eventInfoText.height + extraY
            )
        }

        val image = surface.makeImageSnapshot()
        val data = image.encodeToData(EncodedImageFormat.JPEG, QUALITY)
            ?: return buildMessageWrapper {
                appendText("生成图片失败!")
            }

        return buildMessageWrapper {
            appendElement(data.bytes.asImage())
        }
    }

    /**
     * 绘制歌曲谱面
     *
     * @param musicInfo 歌曲信息 [PJSKMusicInfo]
     * @param difficulty 歌曲难度 [MusicDifficulty]
     *
     * @return 渲染完成的图片路径和错误信息 [File] [String]
     */
    suspend fun drawCharts(
        musicInfo: PJSKMusicInfo,
        difficulty: MusicDifficulty
    ): Pair<File?, String> {
        val chartFile = pjskFolder.resolve("charts/${musicInfo.id}/chart_$difficulty.png")

        if (!chartFile.isBlank() && chartFile.isType("image/png")) {
            return Pair(chartFile, "")
        }

        var chartFiles = ProjectSekaiCharts.getCharts(musicInfo, difficulty)

        if (chartFiles.isEmpty()) {
            if (ProjectSekaiCharts.downloadChart(musicInfo)) {
                chartFiles = ProjectSekaiCharts.getCharts(musicInfo, difficulty)
            } else {
                return Pair(null, "谱面下载中或下载失败, 可能暂未更新")
            }
        }

        val bg = try {
            Image.makeFromEncoded(chartFiles[0].readBytes())
        } catch (e: IllegalArgumentException) {
            return Pair(null, "谱面背景未准备好")
        }
        val bar = try {
            Image.makeFromEncoded(chartFiles[1].readBytes())
        } catch (e: IllegalArgumentException) {
            return Pair(null, "谱面序号表未准备好")
        }
        val chart = try {
            Image.makeFromEncoded(chartFiles[2].readBytes())
        } catch (e: IllegalArgumentException) {
            return Pair(null, "谱面按键未准备好")
        }

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
            val info = ProjectSekaiMusicDifficulty.getMusicDifficulty(musicInfo.id).find {
                it.musicDifficulty == difficulty
            }
            addText(
                "${difficulty.name} [Lv.${
                ProjectSekaiManager.getSongLevel(
                    musicInfo.id,
                    difficulty
                )
                }] | 共 ${info?.totalNoteCount} 个键"
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

        val cover = try {
            ProjectSekaiMusic.getMusicCover(musicInfo).readBytes().let {
                Image.makeFromEncoded(it)
            }
        } catch (e: IllegalArgumentException) {
            return Pair(null, "歌曲封面未准备好")
        }

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

            // Draw music cover
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
            ?: return Pair(null, "生成谱面失败")

        chartFile.touch()
        chartFile.writeBytes(data.bytes)

        return Pair(chartFile, "")
    }
}
