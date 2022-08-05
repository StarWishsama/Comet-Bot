package ren.natsuyuk1.comet.test.graphics

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Surface
import org.jetbrains.skia.paragraph.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.test.isCI
import java.awt.Color
import java.io.File
import java.nio.file.Files
import javax.swing.filechooser.FileSystemView


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDrawGraphics {
    @Test
    fun testDraw() {
        if (isCI()) return

        val surface = Surface.makeRasterN32Premul(300, 400)

        val fonts =
            FontCollection().setDynamicFontManager(TypefaceFontProvider()).setDefaultFontManager(FontMgr.default)

        val typeface = fonts.findTypefaces(arrayOf("Hiragino Sans"), FontStyle.NORMAL.withWeight(500)).firstOrNull()

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            /**val tl = TextLine.make("星期日 番剧列表", Font(typeface, 22f))
            drawTextLine(tl, 10f, 30f, Paint().apply {
            color = Color.BLACK.rgb
            })*/

            val testName = listOf("00:00 >> Lycoris Recoil", "01:00 >> Engage Kiss", "17:30 >> LoveLive Superstar!!")

            val builder = ParagraphBuilder(ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = TextStyle().apply {
                    color = Color.BLACK.rgb
                    fontSize = 20f
                }
            }, fonts)

            builder.addText("星期日 番剧列表\n\n")

            testName.forEach {
                builder.addText(it + "\n")
            }

            builder.build().layout(300f).paint(this, 10f, 10f)
        }

        val image = surface.makeImageSnapshot()

        val imageData = image.encodeToData(EncodedImageFormat.PNG)
        imageData?.bytes?.let {
            Files.write(
                File(FileSystemView.getFileSystemView().homeDirectory, "test.png").toPath(),
                it
            )
        }
    }
}
