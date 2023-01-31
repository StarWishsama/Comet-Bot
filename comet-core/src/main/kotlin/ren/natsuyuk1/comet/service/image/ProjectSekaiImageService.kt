package ren.natsuyuk1.comet.service.image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import moe.sdl.yac.core.PrintMessage
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
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
import ren.natsuyuk1.comet.utils.ktor.DownloadStatus
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.math.NumberUtil.toInstant
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import ren.natsuyuk1.comet.utils.skiko.FontUtil.gloryFontSetting
import ren.natsuyuk1.comet.utils.skiko.addTextln
import ren.natsuyuk1.comet.utils.skiko.changeStyle
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
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

    private const val BEST30_HEIGHT = 1900
    private const val BEST30_WIDTH = 940

    /**
     * 绘制一张玩家的 30 首最佳歌曲表
     *
     * @param user 从 API 获得的 [ProjectSekaiUserInfo]
     * @param b30 表现最佳的 30 首歌
     *
     * @return 渲染后图片 [Image]
     */
    suspend fun drawBest30(
        user: ProjectSekaiUserInfo,
        b30: List<ProjectSekaiUserInfo.MusicResult>
    ): ren.natsuyuk1.comet.api.message.Image {
        val cardId = user.userDecks.first().leader
        val avatarBundleName = ProjectSekaiCard.getAssetBundleName(cardId) ?: throw PrintMessage("加载卡面数据失败")
        val status =
            user.userCards.find { it.cardId == cardId }?.specialTrainingStatus ?: throw PrintMessage("加载卡面数据失败")

        val background = Image.makeFromEncoded(pjskFolder.resolve("b30/b30-background.png").readBytes())

        val avatar = Image.makeFromEncoded(
            ProjectSekaiManager.resolveCardImage(
                avatarBundleName, status
            ).readBytes()
        )

        val surface = Surface.makeRasterN32Premul(BEST30_WIDTH, BEST30_HEIGHT)

        surface.canvas.apply {
            drawImage(background, 0f, 0f, Paint().apply { isAntiAlias = true })

            drawAvatar(avatar, 25f, 35f, 128f, 10f)

            ParagraphBuilder(
                ParagraphStyle().apply {
                    alignment = Alignment.LEFT
                    textStyle = FontUtil.defaultFontStyle(Color.BLACK, 35f, style = FontStyle.BOLD)
                    gloryFontSetting()
                },
                FontUtil.fonts
            ).apply {
                addTextln(user.user.userGameData.name.limit(12, ".."))
                changeStyle(FontUtil.defaultFontStyle(Color.BLACK, 20f))
                addText("ID: ${user.user.userGameData.userID}")
            }.build().layout(366f)
                .paint(this, 180f, 45f)

            // drawBadge()

            // 左右间隔 10, 上下间隔 20

            var x = 25f
            var y = 260f

            val infoH = 125f
            val infoW = 290f

            b30.forEachIndexed { i, musicResult ->
                drawMusicInfo(musicResult, x, y)

                if ((i + 1) % 3 == 0) {
                    x = 25f
                    y += (infoH + 20f)
                } else {
                    x += (infoW + 10f)
                }
            }

            drawTextLine(
                TextLine.make(
                    "歌曲难度数据来源于 Project Sekai Profile",
                    FontUtil.defaultFont(20f, style = FontStyle.BOLD)
                ),
                25f,
                1850f,
                Paint().apply {
                    color = Color.BLACK.rgb
                    isAntiAlias = true
                }
            )

            drawTextLine(
                TextLine.make(
                    "Render by Comet",
                    FontUtil.defaultFont(20f, style = FontStyle.BOLD)
                ),
                750f,
                1850f,
                Paint().apply {
                    color = Color.BLACK.rgb
                    isAntiAlias = true
                }
            )
        }

        val img = surface.makeImageSnapshot()

        return ren.natsuyuk1.comet.api.message.Image(
            byteArray = img.encodeToData(EncodedImageFormat.PNG, QUALITY)?.bytes
                ?: throw PrintMessage("生成 Best 30 图片失败")
        )
    }

    private fun Canvas.drawAvatar(avatar: Image, x: Float, y: Float, size: Float, radius: Float) {
        val rrect = RRect.makeXYWH(
            x, y, size, size, radius // 圆形弧度
        )

        save()
        clipRRect(rrect, true)
        drawImageRect(
            avatar,
            Rect(0f, 0f, avatar.width.toFloat(), avatar.height.toFloat()),
            rrect,
            SamplingMode.MITCHELL,
            Paint().apply { isAntiAlias = true },
            true
        )
        restore()
    }

    private fun Canvas.drawBadge(badge: Image, x: Float, y: Float, size: Float, radius: Float) {
        TODO()
    }

    private suspend fun Canvas.drawMusicInfo(
        musicResult: ProjectSekaiUserInfo.MusicResult,
        x: Float,
        y: Float
    ) {
        // 290 x 125
        save()
        drawRect(Rect.makeXYWH(x, y, 290f, 125f), Paint().apply { color = Color.WHITE.rgb })
        restore()

        val musicInfo = ProjectSekaiMusic.getMusicInfo(musicResult.musicId) ?: return
        val musicLevel = ProjectSekaiManager.getSongAdjustedLevel(
            musicResult.musicId, musicResult.musicDifficulty, musicResult.playResult
        )
        val coverFile = ProjectSekaiMusic.getMusicCover(musicInfo)

        val cover = try {
            Image.makeFromEncoded(coverFile.readBytes())
        } catch (e: Exception) {
            return
        }

        val rrect = RRect.makeXYWH(
            x + 20f, y + 20f, 80f, 80f, 0f
        )

        save()
        clipRRect(rrect, true)
        drawImageRect(
            cover,
            Rect(0f, 0f, cover.width.toFloat(), cover.height.toFloat()),
            rrect,
            CubicResampler(1 / 3.0f, 1 / 3.0f),
            Paint().apply { isAntiAlias = true },
            true
        )
        restore()

        drawCircle(
            x + 20f, y + 20f, 15f,
            Paint().apply {
                color = if (musicResult.musicDifficulty == MusicDifficulty.MASTER) {
                    Color(187, 51, 238).rgb
                } else {
                    Color(238, 67, 102).rgb
                }
            }
        )

        drawString(
            musicLevel?.fixDisplay(0) ?: "N/A",
            x + 11f,
            y + 20f + 4.5f,
            FontUtil.defaultFont(15f, style = FontStyle.BOLD),
            Paint().apply {
                color = Color.WHITE.rgb
                isAntiAlias = true
            }
        )

        drawTextLine(
            TextLine.make(
                musicInfo.title.limit(8, ".."),
                FontUtil.defaultFont(20f, style = FontStyle.BOLD)
            ),
            x + 115f,
            y + 35f,
            Paint().apply {
                color = Color.BLACK.rgb
                mode = PaintMode.FILL
                isAntiAlias = true
            }
        )

        // PJSK Profile player score 算法
        val multipier = when (musicResult.playResult) {
            MusicPlayResult.ALL_PERFECT -> 8.0
            MusicPlayResult.FULL_COMBO -> 7.5
            MusicPlayResult.CLEAR -> 5.0
        }

        drawTextLine(
            TextLine.make(
                "${musicLevel?.fixDisplay(1) ?: "N/A"} " +
                    "${if (musicLevel != null) " → " + (musicLevel * multipier).toInt() else ""}",
                FontUtil.defaultFont(15f)
            ),
            x + 115f,
            y + 65f,
            Paint().apply {
                color = Color.BLACK.rgb
                isAntiAlias = true
            }
        )

        val statusImg = Image.makeFromEncoded(
            withContext(Dispatchers.IO) {
                pjskFolder.resolve(
                    if (musicResult.playResult == MusicPlayResult.ALL_PERFECT) "b30/AllPerfect.png"
                    else "b30/FullCombo.png"
                ).readBytes()
            }
        )

        val statusRect = Rect.makeXYWH(
            x + 112f, y + 75f, 165f, 30f
        )

        save()
        clipRect(statusRect, true)
        drawImageRect(
            statusImg,
            Rect(0f, 0f, statusImg.width.toFloat(), statusImg.height.toFloat()),
            statusRect.inflate(-1f),
            SamplingMode.MITCHELL,
            Paint().apply { isAntiAlias = true },
            true
        )
        restore()
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
                        (eventInfo.aggregateTime.toInstant(true) - now).toFriendly(TimeUnit.SECONDS)
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
                val teamName = ProjectSekaiI18N.getCarnivalTeamName(profile.userCheerfulCarnival.cheerfulCarnivalTeamId)

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

        val surface = Surface.makeRasterN32Premul(
            WIDTH,
            (
                AVATAR_SIZE + DEFAULT_PADDING * 2.5 + eventInfoText.height + eventScoreText.height + (
                    eventTeamText?.height
                        ?: 0f
                    )
                ).toInt()
        )

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            if (avatar != null) {
                val rrect = RRect.makeXYWH(
                    20f, 20f, AVATAR_SIZE.toFloat(), AVATAR_SIZE.toFloat(), 10f // 圆形弧度
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
                this, DEFAULT_PADDING * 2f + AVATAR_SIZE, DEFAULT_PADDING.toFloat()
            )

            eventInfoText.paint(
                this, DEFAULT_PADDING.toFloat(), (AVATAR_SIZE + DEFAULT_PADDING * 2).toFloat()
            )

            var extraY = 0f

            if (eventInfo.eventType == "cheerful_carnival" &&
                profile.userCheerfulCarnival.cheerfulCarnivalTeamId != null &&
                eventTeamText != null
            ) {
                val teamNum = if (profile.userCheerfulCarnival.cheerfulCarnivalTeamId % 2 == 0) 2 else 1
                val teamIcon = ProjectSekaiEvent.getEventTeamImage(teamNum)
                var extraX = 0f

                if (teamIcon != null) {
                    val teamIconImg = Image.makeFromEncoded(teamIcon.readBytes())
                    val rect = Rect.makeXYWH(
                        DEFAULT_PADDING.toFloat(), AVATAR_SIZE + DEFAULT_PADDING * 2 + eventInfoText.height, 30f, 30f
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
                    this, DEFAULT_PADDING + extraX + 10f, AVATAR_SIZE + DEFAULT_PADDING * 2 + eventInfoText.height
                )

                extraY = eventTeamText.height
            }

            eventScoreText.paint(
                this, DEFAULT_PADDING.toFloat(), AVATAR_SIZE + DEFAULT_PADDING * 2 + eventInfoText.height + extraY
            )
        }

        val image = surface.makeImageSnapshot()
        val data = image.encodeToData(EncodedImageFormat.JPEG, QUALITY) ?: return buildMessageWrapper {
            appendText("生成图片失败!")
        }

        return buildMessageWrapper {
            appendElement(data.bytes.asImage())
        }
    }

    /**
     * 绘制歌曲谱面
     *
     * @param musicInfo 歌曲信息 [ProfileMusicInfo]
     * @param difficulty 歌曲难度 [MusicDifficulty]
     *
     * @return 渲染完成的图片路径和错误信息 [File] [String]
     */
    suspend fun drawCharts(
        musicInfo: ProjectSekaiMusicInfo,
        difficulty: MusicDifficulty
    ): Pair<File?, String> {
        val chartFile = musicInfo.id.chart(difficulty)

        if (!chartFile.isBlank() && chartFile.isType("image/png")) {
            return Pair(chartFile, "")
        }

        if (musicInfo.id.chartKey(difficulty).let { it.isBlank() || it.isType("image/png") }) {
            when (ProjectSekaiCharts.downloadChart(musicInfo)) {
                DownloadStatus.UNVERIFIED, DownloadStatus.FAILED -> {
                    return Pair(null, "谱面下载失败, 可能谱面暂未更新或是网络问题")
                }

                DownloadStatus.DOWNLOADING -> {
                    return Pair(null, "已有相同谱面正在下载中, 请稍等")
                }

                else -> {} // OK
            }
        }

        val bg = try {
            Image.makeFromEncoded(musicInfo.id.bg().readBytes())
        } catch (e: IllegalArgumentException) {
            return Pair(null, "谱面背景未准备好")
        }

        val bar = try {
            Image.makeFromEncoded(musicInfo.id.bar().readBytes())
        } catch (e: IllegalArgumentException) {
            return Pair(null, "谱面序号表未准备好")
        }

        val chart = try {
            Image.makeFromEncoded(musicInfo.id.chartKey(difficulty).readBytes())
        } catch (e: IllegalArgumentException) {
            return Pair(null, "谱面未准备好")
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
                    musicInfo.id, difficulty
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
            bg.width + DEFAULT_PADDING, bg.height + DEFAULT_PADDING * 3 + text.height.toInt()
        )

        val cover = try {
            Image.makeFromEncoded(ProjectSekaiMusic.getMusicCover(musicInfo).readBytes())
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
                30f, 40f + bg.height, text.height, text.height, 0f
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

        val data = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG) ?: return Pair(null, "生成谱面失败")

        chartFile.touch()
        chartFile.writeBytes(data.bytes)

        return Pair(chartFile, "")
    }
}
