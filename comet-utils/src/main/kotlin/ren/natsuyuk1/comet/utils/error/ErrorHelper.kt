package ren.natsuyuk1.comet.utils.error

import mu.KotlinLogging
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

object ErrorHelper {
    private val errorReportsFolder = resolveDirectory("error-reports")
    private val standardDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    suspend fun createErrorReportFile(
        reason: String = "发生了一个错误",
        type: String,
        t: Throwable?,
        message: String
    ) {
        val fileName = "$type-${standardDateTimeFormatter.format(LocalDateTime.now())}.txt"
        val location = File(errorReportsFolder, fileName)

        location.touch()

        val report =
            """
            发生了一个错误:
            ${if (t != null) getBeautyStackTrace(t) else "无报错堆栈"}
            
            额外信息:
            $message    
            """.trimIndent()

        location.writeTextBuffered(report)
        logger.warn("$reason, 错误报告已生成! 保存在 ${location.path}")
        logger.warn("如果你无法确定原因, 可以将其反馈到 https://github.com/StarWishsama/Comet-Bot/issues")
        logger.warn("以下是错误堆栈: ", t)
    }

    /**
     * https://github.com/Polar-Pumpkin/ParrotX/blob/master/src/main/java/org/serverct/parrot/parrotx/utils/I18n.java#L328
     *
     * 更优雅的堆栈跟踪输出.
     *
     * @author Polar-Pumpkin
     * @param exception Throwable 类型的异常
     *
     * @return 格式化后的栈轨迹报告
     */
    private fun getBeautyStackTrace(exception: Throwable): String {
        val sb = StringBuilder()
        sb.append("========================= 栈轨迹 =========================\n")
        sb.append("异常类型 ▶\n")
        sb.append(exception.toString() + "\n")
        sb.append("\n")
        var lastPackage = ""
        for (elem in exception.stackTrace) {
            val key = elem.className
            val nameSet = key.split("[.]".toRegex()).toTypedArray()
            val className = nameSet[nameSet.size - 1]
            val packageSet = arrayOfNulls<String>(nameSet.size - 2)
            System.arraycopy(nameSet, 0, packageSet, 0, nameSet.size - 2)
            val packageName = StringBuilder()
            for ((counter, nameElem) in packageSet.withIndex()) {
                packageName.append(nameElem)
                if (counter < packageSet.size - 1) {
                    packageName.append(".")
                }
            }

            if (packageName.toString() != lastPackage) {
                lastPackage = packageName.toString()
                sb.append("\n")
                sb.append("包名 $packageName ▶\n")
            }
            sb.append("  ▶ 在类 " + className + ", 方法 " + elem.methodName + ". (" + elem.fileName + ", 行 " + elem.lineNumber + ")" + "\n")

        }
        sb.append("========================= 栈轨迹 =========================\n")

        return sb.toString()
    }
}
