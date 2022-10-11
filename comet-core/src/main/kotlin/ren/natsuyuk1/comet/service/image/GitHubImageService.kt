package ren.natsuyuk1.comet.service.image

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.objects.github.events.GithubEventData
import ren.natsuyuk1.comet.objects.github.events.PullRequestEventData
import ren.natsuyuk1.comet.objects.github.events.PushEventData
import ren.natsuyuk1.comet.service.refsPattern
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import java.awt.Color
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import kotlin.time.Duration.Companion.hours

object GitHubImageService {
    private val resourceFile = resolveResourceDirectory("github")
    private val githubLogo = resourceFile.resolve("github_logo.png")
    private const val GITHUB_CONTENT_PADDING = 10f
    private const val GITHUB_CONTENT_MARGIN = 10f
    private const val GITHUB_DEFAULT_WIDTH = 600

    fun drawEventInfo(event: GithubEventData): File {
        return when (event) {
            is PullRequestEventData -> {
                event.draw()
            }

            is PushEventData -> {
                event.draw()
            }

            else -> error("不支持转换的事件, 请使用文本转换.")
        }
    }

    private suspend fun generateTempImageFile(event: GithubEventData): File {
        return File(cacheDirectory, "${System.currentTimeMillis()}-${event.type()}.png").apply {
            TaskManager.registerTaskDelayed(1.hours) {
                delete()
            }

            touch()
        }
    }

    private fun checkResource() {
        if (!githubLogo.exists())
            throw FileNotFoundException("找不到 GitHub Logo 文件, 请手动下载 GitHub 资源包文件解压在 ./resources/github")
    }

    private fun drawHeader(firstLine: String, secondLine: String, width: Float): Paragraph =
        ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 21f)
            },
            FontUtil.fonts
        ).apply {
            addText(firstLine)
            addText(secondLine)
        }.build().layout(width)

    private fun drawPadding(width: Float): Paragraph =
        ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 15f)
            },
            FontUtil.fonts
        ).apply {
            addText("☄ Rendered by Comet")
        }.build().layout(width)

    private fun drawBody(eventContent: String, width: Float): Paragraph =
        ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 18f)
            },
            FontUtil.fonts
        ).apply {
            addText(eventContent)
        }.build().layout(width)

    private fun drawBody(width: Float, customBuilder: ParagraphBuilder.() -> Unit): Paragraph =
        ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 18f)
            },
            FontUtil.fonts
        ).apply {
            customBuilder(this)
        }.build().layout(width)

    private fun PullRequestEventData.draw(): File {
        checkResource()

        val image = Image.makeFromEncoded(githubLogo.readBytes())

        val repoInfo = drawHeader(
            "🔧 ${repository.fullName} 有新提交更改\n",
            "由 ${sender.login} 创建于 ${pullRequestInfo.convertCreatedTime()}",
            GITHUB_DEFAULT_WIDTH - GITHUB_CONTENT_MARGIN * 2 - image.width
        )

        val pullRequestBody = drawBody(GITHUB_DEFAULT_WIDTH - GITHUB_CONTENT_MARGIN * 2) {
            addText("📜 ${pullRequestInfo.title}\n\n")

            popStyle()
            pushStyle(FontUtil.defaultFontStyle(Color.BLACK, 16f))

            addText((pullRequestInfo.body ?: "没有描述").limit(400))
        }

        val padding = drawPadding(GITHUB_DEFAULT_WIDTH - GITHUB_CONTENT_MARGIN * 2)

        val height = (pullRequestBody.height + padding.height * 2.5 + image.height).toInt()

        val surface = Surface.makeRasterN32Premul(
            GITHUB_DEFAULT_WIDTH,
            height
        )

        surface.canvas.apply {
            clear(Color.WHITE.rgb)
            // Draw github logo
            drawImage(image, GITHUB_CONTENT_PADDING, GITHUB_CONTENT_MARGIN)

            repoInfo.paint(this, GITHUB_CONTENT_PADDING * 2.5f + image.width, GITHUB_CONTENT_MARGIN)
            pullRequestBody.paint(this, GITHUB_CONTENT_PADDING, GITHUB_CONTENT_MARGIN * 2 + image.height)
            padding.paint(
                this,
                GITHUB_CONTENT_PADDING,
                GITHUB_CONTENT_MARGIN * 2 + image.height + pullRequestBody.height
            )
        }

        val tempFile = runBlocking { generateTempImageFile(this@draw) }

        surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)?.bytes?.let {
            Files.write(tempFile.toPath(), it)
        }

        return tempFile
    }

    private fun PushEventData.draw(): File {
        checkResource()

        if (headCommitInfo == null || commitInfo.isEmpty()) {
            throw IllegalStateException("提交者信息不应为空")
        }

        val image = Image.makeFromEncoded(githubLogo.readBytes())

        val repoInfo = drawHeader(
            "⬆️ ${repoInfo.fullName} [${ref.replace(refsPattern, "")}] 有新推送\n",
            "由 ${headCommitInfo.committer.name} 提交于 ${getPushTimeAsString()}",
            GITHUB_DEFAULT_WIDTH - GITHUB_CONTENT_MARGIN * 2 - image.width
        )

        val pushBody = drawBody(buildCommitList(), GITHUB_DEFAULT_WIDTH - GITHUB_CONTENT_MARGIN * 2)

        val padding = drawPadding(GITHUB_DEFAULT_WIDTH - GITHUB_CONTENT_MARGIN * 2)

        val height = (pushBody.height + padding.height * 2.5 + image.height).toInt()

        val surface = Surface.makeRasterN32Premul(
            GITHUB_DEFAULT_WIDTH,
            height
        )

        surface.canvas.apply {
            clear(Color.WHITE.rgb)
            // Draw github logo
            drawImage(image, GITHUB_CONTENT_PADDING, GITHUB_CONTENT_MARGIN)

            repoInfo.paint(this, GITHUB_CONTENT_PADDING * 2.5f + image.width, GITHUB_CONTENT_MARGIN)
            pushBody.paint(this, GITHUB_CONTENT_PADDING, GITHUB_CONTENT_MARGIN * 2 + image.height)
            padding.paint(this, GITHUB_CONTENT_PADDING, GITHUB_CONTENT_MARGIN * 2 + image.height + pushBody.height)
        }

        val tempFile = runBlocking { generateTempImageFile(this@draw) }

        surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)?.bytes?.let {
            Files.write(tempFile.toPath(), it)
        }

        return tempFile
    }
}
