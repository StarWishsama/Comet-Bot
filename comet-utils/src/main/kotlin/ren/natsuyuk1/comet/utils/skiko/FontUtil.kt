package ren.natsuyuk1.comet.utils.skiko

import mu.KotlinLogging
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory
import java.awt.Color

private val logger = KotlinLogging.logger {}

object FontUtil {
    private val fontMgr = FontMgr.default
    private val fontProvider = TypefaceFontProvider()
    private val defaultFont = arrayOf("Source Han Sans SC", "Twemoji Mozilla")

    val fonts = FontCollection().setDynamicFontManager(fontProvider).setDefaultFontManager(fontMgr)

    private val fontFolder = resolveResourceDirectory("/fonts")

    fun loadDefaultFont() {
        if (!fontFolder.exists()) {
            fontFolder.mkdirs()
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

    fun defaultFont(
        size: Float,
        fontFamily: Array<String> = defaultFont,
        style: FontStyle = FontStyle.NORMAL.withWeight(500)
    ): Font {
        val typeface =
            fonts.findTypefaces(
                familyNames = fontFamily,
                style = style
            ).firstOrNull()
        return Font(typeface, size).apply {
            edging = FontEdging.SUBPIXEL_ANTI_ALIAS
            hinting = FontHinting.FULL
            isSubpixel = true
        }
    }

    fun defaultFontStyle(
        c: Color,
        size: Float,
        fontFamily: Array<String> = defaultFont,
        style: FontStyle = FontStyle.NORMAL.withWeight(500)
    ) = TextStyle().apply {
        color = c.rgb
        fontSize = size
        fontStyle = style
        fontFamilies = fontFamily
    }

    fun ParagraphStyle.gloryFontSetting() = this.apply {
        fontRastrSettings = FontRastrSettings(
            FontEdging.SUBPIXEL_ANTI_ALIAS,
            FontHinting.FULL,
            true
        )
    }
}
