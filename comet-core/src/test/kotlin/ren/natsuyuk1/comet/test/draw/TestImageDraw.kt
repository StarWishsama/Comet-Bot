package ren.natsuyuk1.comet.test.draw

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import java.io.File
import java.nio.file.Files
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestImageDraw {
    @BeforeAll
    fun init() {
        runBlocking {
            SkikoHelper.findSkikoLibrary()
        }
    }

    @Test
    fun test() {
        val surface = Surface.makeRasterN32Premul(650, 650)
        val canvas = surface.canvas

        canvas.apply {
            clear(Color.WHITE)
            val testImage = File("C:\\Users\\NatsuYuki\\Desktop\\arcaea_song_cover\\ddd\\base.jpg")

            // 左上角 (10, 10) 长 40 宽 40
            val src = Rect(10f, 10f, 110f, 110f)

            // only dst
            drawImageRect(Image.makeFromEncoded(testImage.readBytes()), src)

            val dst = Rect(20f, 20f, 256f, 256f)
            //drawImageRect(Image.makeFromEncoded(testImage.readBytes()), testRect, dst)
            //drawImage(Image.makeFromEncoded(testImage.readBytes()), 10f, 10f)
        }

        val image = surface.makeImageSnapshot()

        val tmpFile = File("C:\\Users\\NatsuYuki\\Desktop\\test.png")
        tmpFile.createNewFile()

        image.encodeToData(EncodedImageFormat.PNG)?.bytes?.let {
            Files.write(tmpFile.toPath(), it)
        }

        println(tmpFile)
    }
}
