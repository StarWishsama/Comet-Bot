package ren.natsuyuk1.comet.test.draw

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toJavaInstant
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.twitter.client
import ren.natsuyuk1.comet.network.thirdparty.twitter.initSetsuna
import ren.natsuyuk1.comet.objects.config.TwitterConfig
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.comet.utils.time.hourMinutePattern
import ren.natsuyuk1.comet.utils.time.yyMMddPattern
import ren.natsuyuk1.setsuna.api.fetchTweet
import ren.natsuyuk1.setsuna.api.fetchUser
import ren.natsuyuk1.setsuna.api.options.Expansions
import ren.natsuyuk1.setsuna.api.options.defaultTwitterOption
import ren.natsuyuk1.setsuna.api.options.defaultUserOption
import ren.natsuyuk1.setsuna.response.TweetFetchResponse
import ren.natsuyuk1.setsuna.util.removeShortLink
import java.awt.Color
import java.io.File
import java.nio.file.Files
import kotlin.coroutines.EmptyCoroutineContext

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestTweetDraw {
    @BeforeAll
    fun init() {
        if (isCI()) return

        runBlocking {
            SkikoHelper.findSkikoLibrary()
            TwitterConfig.init()
            initSetsuna(EmptyCoroutineContext)
        }
    }

    @Test
    fun test() {
        if (isCI()) return

        runBlocking {
            client.fetchTweet("1571845248627937280", defaultTwitterOption + Expansions.Media())
        }.drawTweet()
    }

    private fun TweetFetchResponse.drawTweet() {
        val defaultWidth = 800f
        val defaultMargin = 10f
        val defaultPadding = 20f

        val defaultAvatarSize = 72f

        val defaultTextSpace = 20

        val widthWithoutPadding = defaultWidth - defaultMargin * 2

        val tweetContent = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 30f)
            },
            FontUtil.fonts
        ).apply {
            addText(tweet!!.text.removeShortLink())
        }.build().layout(widthWithoutPadding)

        val padding = ParagraphBuilder(
            ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 20f)
            },
            FontUtil.fonts
        ).apply {
            val sentTime = tweet!!.createdTime!!.toJavaInstant()
            addText(hourMinutePattern.format(sentTime) + " | " + yyMMddPattern.format(sentTime) + " | Rendered by Comet")
        }.build().layout(widthWithoutPadding)

        val tweetImage = includes?.media?.first()

        val user = runBlocking {
            client.fetchUser(tweet!!.authorID!!, defaultUserOption).user
        }!!

        val avatarPath = File("./test_avatar.png").also {
            runBlocking {
                it.touch()
            }
        }

        val outputPath = File("./output.png").also {
            runBlocking {
                it.touch()
            }
        }

        val tweetPicturePath = File("./tweet_picture.png").also {
            runBlocking {
                it.touch()
            }
        }

        val tweetPicture = Image.makeFromEncoded(tweetPicturePath.readBytes())

        val textHeight = tweetContent.height

        println(textHeight)

        val height = (defaultAvatarSize +
            textHeight +
            tweetPicture.height +
            padding.height +
            defaultPadding
        ).toInt()

        println("Actual height $height")

        runBlocking {
           // ren.natsuyuk1.comet.test.network.client.client.downloadFile(user.profileImageURL!!.replace("_normal", ""), avatarPath)
           // ren.natsuyuk1.comet.test.network.client.client.downloadFile(tweetImage!!.url!!, tweetPicturePath)
        }

        val surface = Surface.makeRasterN32Premul(defaultWidth.toInt(), height)

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            val tarFaceRect = RRect.makeXYWH(
                defaultMargin * 1.8f,
                defaultMargin * 1.2f,
                defaultAvatarSize,
                defaultAvatarSize,
                defaultAvatarSize / 2
            )

            drawCircle(
                tarFaceRect.left + tarFaceRect.width / 2,
                tarFaceRect.top + tarFaceRect.width / 2,
                tarFaceRect.width / 2,
                Paint().apply { color = Color.BLACK.rgb })

            val image = Image.makeFromEncoded(avatarPath.readBytes())

            drawImageRRect(image, tarFaceRect)

            val userInfo = ParagraphBuilder(
                ParagraphStyle().apply {
                    alignment = Alignment.LEFT
                    textStyle = FontUtil.defaultFontStyle(Color.BLACK, 25f)
                },
                FontUtil.fonts
            ).apply {
                addText(user.name.limit(16))
                addText("\n")
                popStyle()
                pushStyle(FontUtil.defaultFontStyle(Color.GRAY, 22f))
                addText("@${user.username}")
            }.build().layout(defaultWidth - defaultAvatarSize)

            userInfo.paint(this, defaultAvatarSize + defaultMargin * 2 + defaultTextSpace, (defaultMargin + defaultTextSpace) / 2)

            tweetContent.paint(this, tarFaceRect.left + defaultMargin, defaultAvatarSize + defaultMargin + defaultTextSpace)

            resizeTweetImage(tarFaceRect.left + defaultMargin, defaultAvatarSize + defaultMargin + tweetContent.height + defaultTextSpace, tweetPicture)

            padding.paint(this, tarFaceRect.left + defaultMargin, (height - defaultPadding - padding.height))
        }

        val image = surface.makeImageSnapshot()

        image.encodeToData(EncodedImageFormat.PNG)?.bytes?.let {
            Files.write(outputPath.toPath(), it)
        }

        println("Finished output: ${outputPath.absolutePath}")
    }
}

/**
 * 绘制圆角图片
 */
fun Canvas.drawImageRRect(image: Image, rRect: RRect, paint: Paint? = null) {
    save()
    clipRRect(rRect, true)
    drawImageRect(image, Rect(0f, 0f, image.width.toFloat(), image.height.toFloat()), rRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), paint, true)
    restore()
}

fun Canvas.resizeTweetImage(x: Float, y: Float, vararg image: Image): Rect {
    return when (image.size) {
        1 -> {
            val tarImageRect = Rect.makeXYWH(
                x * 1.2f,
                y * 1.2f,
                600f,
                300f
            )

            drawImageRect(image.first(), tarImageRect)

            tarImageRect
        }
        2 -> {
            TODO()
        }
        3 -> {
            TODO()
        }
        4 -> {
            TODO()
        }

        else -> error("Wrong image size: ${image.size}, it should be [1, 4]")
    }
}
