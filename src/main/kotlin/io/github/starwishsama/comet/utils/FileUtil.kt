package io.github.starwishsama.comet.utils

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import cn.hutool.core.net.URLDecoder
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.Comet
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun File.writeClassToJson(context: Any) {
    if (!this.exists()) {
        this.createNewFile()
    }

    FileWriter.create(this).write(BotVariables.gson.toJson(context))
}

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
 * 直接将文件内容序列化为指定的类
 *
 * @param clazz 指定类
 * @return 指定类
 */
fun <T> File.parseAsClass(clazz: Class<T>): T {
    require(exists())
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

    fun getResourceFolder(): File = getChildFolder("res")

    private fun getErrorReportFolder(): File = getChildFolder("error-reports")

    fun createErrorReportFile(type: String, t: Throwable, content: String, url: String) {
        createErrorReportFile("发生了一个错误", type, t, content, "request url: $url")
    }

    fun createErrorReportFile(reason: String, type: String, t: Throwable, content: String, message: String) {
        val fileName = "$type-${dateFormatter.format(LocalDateTime.now())}.txt"
        val location = File(getErrorReportFolder(), fileName)
        if (location.exists()) return

        location.createNewFile()

        val report = "Error occurred:\nExtra message: $message\n${getBeautyStackTrace(t)}\n\nRaw content:\n$content"
        location.writeString(report)
        daemonLogger.info("$reason, 错误报告已生成! 保存在 ${location.path}")
        daemonLogger.info("你可以将其反馈到 https://github.com/StarWishsama/Comet-Bot/issues")
    }

    fun initLog() {
        try {
            val initTime = LocalDateTime.now()
            val parent = getChildFolder("logs")
            BotVariables.log = File(parent, "log-${dateFormatter.format(initTime)}.log")
            BotVariables.log.createNewFile()
        } catch (e: IOException) {
            daemonLogger.error("初始化 Log 文件失败")
        }
    }

    fun getJarLocation(): File {
        var path: String = Comet::class.java.protectionDomain.codeSource.location.path
        if (System.getProperty("os.name").toLowerCase().contains("dows")) {
            path = path.substring(1)
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("/"))
            return File(URLDecoder.decode(path, Charsets.UTF_8))
        }
        return File(URLDecoder.decode(path.replace("target/classes/", ""), Charsets.UTF_8))
    }

    /**
     * https://github.com/Polar-Pumpkin/ParrotX/blob/master/src/main/java/org/serverct/parrot/parrotx/utils/I18n.java#L328
     *
     * 更优雅的 StackTrace 输出.
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

    fun createBlankFile(location: File): File {
        if (!location.exists()) location.createNewFile()
        return location
    }

    fun initResourceFile() {
        val resourcePath = "resources"
        val jarFile = File(Comet.javaClass.protectionDomain.codeSource.location.path)

        if (jarFile.isFile) {
            copyFromJar(jarFile.toPath(), resourcePath, getResourceFolder().toPath())
        } else { // Run with IDE
            val url: URL = Comet::class.java.getResource(resourcePath)
            val apps = File(url.toURI())
            for (app in apps.listFiles() ?: return) {
                copyFolder(app, getResourceFolder())
            }
        }
    }

    /**
     * 复制文件/文件夹至目标位置
     */
    private fun copyFolder(source: File, target: File) {
        if (source.isDirectory) {
            if (!target.exists()) {
                target.mkdir()
            }

            source.list()?.forEach { file ->
                val srcFile = File(source, file)
                val destFile = File(target, file)
                // 递归复制
                copyFolder(srcFile, destFile)
            }
        } else {
            Files.copy(source.toPath(), target.toPath())
        }
    }

    /**
     * 从 jar 中取出文件/文件夹到指定位置
     */
    fun copyFromJar(jarFile: Path, source: String, target: Path) {
        val fileSystem = FileSystems.newFileSystem(jarFile, null)
        val jarPath: Path = fileSystem.getPath(source)

        Files.walkFileTree(jarPath, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                val currentTarget = target.resolve(jarPath.relativize(dir).toString())
                if (!currentTarget.toFile().exists()) {
                    Files.createDirectories(currentTarget)
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val copyTarget = target.resolve(jarPath.relativize(file).toString())
                if (!copyTarget.toFile().exists()) {
                    Files.copy(file, copyTarget, StandardCopyOption.REPLACE_EXISTING)
                }
                return FileVisitResult.CONTINUE
            }
        })

        fileSystem.close()
    }
}