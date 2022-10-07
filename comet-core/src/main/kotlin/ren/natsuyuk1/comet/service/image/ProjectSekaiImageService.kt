package ren.natsuyuk1.comet.service.image

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiUserInfo
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import java.awt.Color
import java.io.File
import java.nio.file.Files
import kotlin.time.Duration.Companion.hours

object ProjectSekaiImageService {
    fun drawB30(user: ProjectSekaiUserInfo.UserGameData, b30: List<ProjectSekaiUserInfo.MusicResult>): File {
        val b30Text = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 20f)
            },
            FontUtil.fonts
        ).apply {
            addText("${user.userGameData.name} - ${user.userGameData.userID} - BEST 30\n")

            popStyle().pushStyle(FontUtil.defaultFontStyle(Color.BLACK, 18f))

            addText("\n")

            b30.forEach { mr ->
                val status = if (mr.isAllPerfect) "AP" else "FC"

                addText(
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
                    })\n"
                )
            }

            addText("\n")

            popStyle().pushStyle(FontUtil.defaultFontStyle(Color.BLACK, 13f))

            addText("由 Comet 生成 | 数据来源于 profile.pjsekai.moe")
        }.build().layout(650f)

        val surface = Surface.makeRasterN32Premul(650, (20 + b30Text.height).toInt())

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            b30Text.paint(this, 10f, 10f)
        }

        val image = surface.makeImageSnapshot()

        val tmpFile = File(cacheDirectory, "${System.currentTimeMillis()}-pjsk.png").apply {
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
}
