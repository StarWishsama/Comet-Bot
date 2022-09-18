package ren.natsuyuk1.comet.utils.skiko

import mu.KotlinLogging
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.copyResourceDirectory
import ren.natsuyuk1.comet.utils.file.jar
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory
import java.awt.Color

private val logger = KotlinLogging.logger {}

object FontUtil {
    private val fontMgr = FontMgr.default
    private val fontProvider = TypefaceFontProvider()
    val fonts = FontCollection().setDynamicFontManager(fontProvider).setDefaultFontManager(fontMgr)

    private val fontFolder = resolveResourceDirectory("/fonts")

    suspend fun loadDefaultFont() {
        if (!fontFolder.exists()) {
            fontFolder.mkdirs()

            val source = jar(this::class.java)
            if (source != null) {
                copyResourceDirectory(source, "fonts", fontFolder)
            }
        }

        var counter = 0

        fontFolder.listFiles()?.let { list ->
            list.forEach { f ->
                try {
                    fontProvider.registerTypeface(
                        Typeface.makeFromFile(f.absPath).also {
                            logger.debug { "Init typeface ${it.familyName}" }
                        }
                    )
                    counter++
                } catch (e: Exception) {
                    logger.warn { "无效的字体文件 ${f.absPath}" }
                }
            }
        }

        logger.info { "已加载 $counter 个字体." }
    }

    fun defaultFont(size: Float) = Font(fonts.findTypefaces(arrayOf("Source Han Sans SC"), FontStyle.NORMAL.withWeight(500)).firstOrNull(), size).apply {
        edging = FontEdging.SUBPIXEL_ANTI_ALIAS
    }

    fun defaultFontStyle(c: Color, size: Float) = TextStyle().apply {
        color = c.rgb
        fontSize = size
        fontStyle = FontStyle.NORMAL.withWeight(500)
        fontFamilies = arrayOf("Source Han Sans SC")
    }
}
