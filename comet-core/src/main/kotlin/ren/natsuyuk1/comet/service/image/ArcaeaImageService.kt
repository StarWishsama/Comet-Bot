package ren.natsuyuk1.comet.service.image

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaHelper
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaSongInfo
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaUserInfo
import ren.natsuyuk1.comet.network.thirdparty.arcaea.formatDifficulty
import ren.natsuyuk1.comet.network.thirdparty.arcaea.formatScore
import ren.natsuyuk1.comet.network.thirdparty.arcaea.formatType
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import ren.natsuyuk1.comet.utils.skiko.changeStyle
import java.awt.Color
import java.io.File
import java.nio.file.Files
import kotlin.time.Duration.Companion.hours

object ArcaeaImageService {
    fun drawB38(user: ArcaeaUserInfo, b38: List<ArcaeaSongInfo>): Pair<List<ArcaeaSongInfo>, File> {
        val paragraph = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 20f)
            },
            FontUtil.fonts,
        ).apply {
            addText("${user.data.name} - ${user.data.userID} - BEST 30\n")

            val overallRating = b38.sumOf { it.songResult.first().rating } / b38.size.toDouble()

            addText("平均 Rating >> ${overallRating.fixDisplay()}\n")

            changeStyle(FontUtil.defaultFontStyle(Color.BLACK, 15f))

            addText("\n")

            b38.take(30).forEach { sr ->
                addText("${sr.encodeToString()}\n")
            }

            if (b38.size > 30) {
                addText("\n")
                addText("Overflow\n")

                b38.drop(30).forEach { sr ->
                    addText("${sr.encodeToString()}\n")
                }
            }

            addText("\n")

            changeStyle(FontUtil.defaultFontStyle(Color.BLACK, 13f))

            addText("由 Comet 生成 | 数据来源于 redive.estertion.win")
        }.build().layout(650f)

        val surface = Surface.makeRasterN32Premul(650, (paragraph.height + 20).toInt())

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            paragraph.paint(this, 10f, 10f)
        }

        val image = surface.makeImageSnapshot()

        val tmpFile = File(cacheDirectory, "${System.currentTimeMillis()}-arcaea.png").apply {
            TaskManager.registerTaskDelayed(1.hours) {
                delete()
            }
        }

        runBlocking { tmpFile.touch() }

        image.encodeToData(EncodedImageFormat.PNG)?.bytes?.let {
            Files.write(tmpFile.toPath(), it)
        }

        return Pair(b38, tmpFile)
    }

    private fun ArcaeaSongInfo.encodeToString(): String {
        val mr = songResult.first()
        return (
            "${ArcaeaHelper.getSongNameByID(mr.songID)} [${mr.difficulty.formatDifficulty()} ${mr.constant}] " +
                "${mr.score} ${mr.score.formatScore()} ${mr.clearType.formatType()} | Rating ${mr.rating.fixDisplay()}"
            )
    }
}
