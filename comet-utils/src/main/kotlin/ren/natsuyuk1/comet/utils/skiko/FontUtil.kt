package ren.natsuyuk1.comet.utils.skiko

import mu.KotlinLogging
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.makeFromFile
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory

private val logger = KotlinLogging.logger {}

object FontUtil {
    private val fontMgr = FontMgr.default
    private val fontProvider = TypefaceFontProvider()
    val fonts = FontCollection().setDynamicFontManager(fontProvider).setDefaultFontManager(fontMgr)

    private val fontFolder = resolveResourceDirectory("/fonts")

    fun loadDefaultFont() {
        if (!fontFolder.exists()) {
            fontFolder.mkdirs()
        }

        fontFolder.listFiles()?.let { list ->
            list.forEach { f ->
                try {
                    fontProvider.registerTypeface(Typeface.makeFromFile(f.absPath))
                } catch (e: IllegalArgumentException) {
                    logger.warn { "无效的字体文件 ${f.absPath}" }
                }
            }
        }
    }
}
