package io.github.starwishsama.comet.utils

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.Comet
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Synchronized
fun File.writeClassToJson(context: Any) {
    if (!this.exists()) {
        this.createNewFile()
    }

    FileWriter.create(this).write(BotVariables.gson.toJson(context))
}

@Synchronized
fun File.writeString(context: String) {
    if (!this.exists()) {
        this.createNewFile()
    }

    FileWriter.create(this).write(context)
}

fun File.getContext(): String {
    return FileReader.create(this).readString()
}

/**
 * 直接将文件内容转换为指定的类
 *
 * @param clazz 指定类
 * @return 指定类
 */
fun <T> File.parseAsClass(clazz: Class<T>): T {
    return BotVariables.gson.fromJson(getContext(), clazz)
}

object FileUtil {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    fun getChildFolder(childName: String): File {
        val childFolder = File(BotVariables.filePath.path + File.separator + childName)
        if (!childFolder.exists()) {
            childFolder.mkdirs()
        }
        return childFolder
    }

    fun getCacheFolder(): File = getChildFolder("cache")

    private fun getErrorReportFolder(): File = getChildFolder("error-reports")

    fun createErrorReportFile(type: String, t: Throwable, content: String, url: String) {
        createErrorReportFile("发生了一个错误", type, t, content, url)
    }

    fun createErrorReportFile(reason: String, type: String, t: Throwable, content: String, url: String) {
        val fileName = "$type-${dateFormatter.format(LocalDateTime.now())}.txt"
        val location = File(getErrorReportFolder(), fileName)
        if (location.exists()) return

        location.createNewFile()

        val report = "Error occurred:\nRequested url: $url\n${getBeautyStackTrace(t)}\n\nRaw content:\n$content"
        location.writeString(report)
        BotVariables.logger.debug("$reason, 错误报告已生成! 保存在 ${location.path}")
    }

    fun initLog() {
        try {
            val initTime = LocalDateTime.now()
            val parent = getChildFolder("logs")
            BotVariables.log = File(parent, "log-${dateFormatter.format(initTime)}.log")
            BotVariables.log.createNewFile()
        } catch (e: IOException) {
            error("尝试输出 Log 失败")
        }
    }

    fun getJarLocation(): String {
        var path: String = Comet::class.java.protectionDomain.codeSource.location.path
        if (System.getProperty("os.name").toLowerCase().contains("dows")) {
            path = path.substring(1)
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("/"))
            return path
        }
        val location = File(path.replace("target/classes/", ""))
        return location.path
    }

    /**
     * https://github.com/Polar-Pumpkin/ParrotX/blob/master/src/main/java/org/serverct/parrot/parrotx/utils/I18n.java#L328
     *
     * @author Polar-Pumpkin
     * @param exception Throwable 类型的异常。
     */
    private fun getBeautyStackTrace(exception: Throwable): String {
        val sb = StringBuilder()
        sb.append("========================= StackTrace =========================\n")
        sb.append("Exception Type ▶\n")
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
                sb.append("Package $packageName ▶\n")
            }
            sb.append("  ▶ at Class " + className + ", Method " + elem.methodName + ". (" + elem.fileName + ", Line " + elem.lineNumber + ")" + "\n")

        }
        sb.append("========================= StackTrace =========================\n")

        return sb.toString()
    }

    fun createBlankFile(path: File, child: String) {
        val file = File(path, child)
        createBlankFile(file)
    }

    fun createBlankFile(location: File) {
        if (!location.exists()) location.createNewFile()
    }
}