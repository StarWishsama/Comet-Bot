package ren.natsuyuk1.comet.service.image

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProfileMusicInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiMusicInfo
import ren.natsuyuk1.comet.objects.pjsk.local.*
import ren.natsuyuk1.comet.utils.file.isBlank
import ren.natsuyuk1.comet.utils.file.isType
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.DownloadStatus
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import ren.natsuyuk1.comet.utils.skiko.addTextln
import ren.natsuyuk1.comet.utils.skiko.changeStyle
import java.awt.Color
import java.io.File

/**
 * 负责绘制 Project Sekai 部分功能的图片
 */
object ProjectSekaiImageService {
    /**
     * 代表图片内容的填充大小
     */
    private const val DEFAULT_PADDING = 20

    /**
     * 谱面背景色
     */
    private val BETTER_GRAY_RGB: Int by lazy { Color(220, 220, 220).rgb }

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
        difficulty: MusicDifficulty,
    ): Pair<File?, String> {
        val chartFile = musicInfo.id.chart(difficulty)

        if (!chartFile.isBlank() && chartFile.isType("image/png")) {
            return Pair(chartFile, "")
        }

        if (musicInfo.id.chartKey(difficulty).let { it.isBlank() || !it.isType("image/png") }) {
            if (!ProjectSekaiCharts.hasSDVXChart(musicInfo)) {
                return Pair(null, "对应歌曲谱面譜面保管所暂未更新")
            }

            when (ProjectSekaiCharts.downloadChart(musicInfo)) {
                DownloadStatus.UNVERIFIED, DownloadStatus.FAILED -> {
                    return Pair(null, "谱面下载失败")
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
            TaskManager.run { ProjectSekaiCharts.downloadChart(musicInfo) }
            return Pair(null, "谱面背景文件损坏")
        }

        val bar = try {
            Image.makeFromEncoded(musicInfo.id.bar().readBytes())
        } catch (e: IllegalArgumentException) {
            TaskManager.run { ProjectSekaiCharts.downloadChart(musicInfo) }
            return Pair(null, "谱面序号表文件损坏")
        }

        val chart = try {
            Image.makeFromEncoded(musicInfo.id.chartKey(difficulty).readBytes())
        } catch (e: IllegalArgumentException) {
            TaskManager.run { ProjectSekaiCharts.downloadChart(musicInfo) }
            return Pair(null, "谱面文件损坏")
        }

        val text = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 70f)
            },
            FontUtil.fonts,
        ).apply {
            addTextln(
                "${musicInfo.title} - ${musicInfo.lyricist}",
            )
            changeStyle(FontUtil.defaultFontStyle(Color.BLACK, 38f))
            val info = ProjectSekaiMusicDifficulty.getDifficulty(musicInfo.id, difficulty)
            addText(
                "${difficulty.name} [Lv.${info?.playLevel}] | 共 ${info?.totalNoteCount} 个键",
            )
        }.build().layout((bg.width + DEFAULT_PADDING).toFloat())

        val rightText = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.RIGHT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 35f)
            },
            FontUtil.fonts,
        ).apply {
            addTextln("谱面数据来自 プロセカ譜面保管所")
            addTextln("Render by Comet")
        }.build().layout(bg.width - 250f)

        val surface = Surface.makeRasterN32Premul(
            bg.width + DEFAULT_PADDING,
            bg.height + DEFAULT_PADDING * 3 + text.height.toInt(),
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
                },
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
                0f,
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
                true,
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
