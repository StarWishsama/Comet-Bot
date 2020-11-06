package io.github.starwishsama.comet.utils

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import cn.hutool.core.net.URLDecoder
import cn.hutool.crypto.SecureUtil
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

fun File.getMD5(): String {
    require(exists()) { "文件不存在" }
    return SecureUtil.md5(this)
}

/**
 * 直接将文件内容序列化为指定的类
 *
 * @param clazz 指定类
 * @return 指定类
 */
fun <T> File.parseAsClass(clazz: Class<T>): T {
    require(exists()) { "文件不存在" }
    return BotVariables.gson.fromJson(getContext(), clazz)
}

fun File.getChildFolder(folderName: String): File {
    val childFolder = File(this, folderName)
    if (!childFolder.exists()) {
        childFolder.mkdirs()
    }
    return childFolder
}

fun File.isEmpty(): Boolean {
    if (!isDirectory) return false

    val files = listFiles() ?: return false

    return files.isEmpty()
}

fun File.filesCount(): Int {
    if (!isDirectory) return -1

    val files = listFiles() ?: return -1

    return files.size
}

object FileUtil {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    fun getChildFolder(childName: String): File = BotVariables.filePath.getChildFolder(childName)

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

        val report = "Error occurred:\n${getBeautyStackTrace(t)}\nExtra message: $message\n\nRaw content:\n$content"
        location.writeString(report)
        daemonLogger.info("$reason, 错误报告已生成! 保存在 ${location.path}")
        daemonLogger.info("你可以将其反馈到 https://github.com/StarWishsama/Comet-Bot/issues")
    }

    fun initLog(): File? {
        try {
            val initTime = LocalDateTime.now()
            val parent = getChildFolder("logs")
            BotVariables.log = File(parent, "log-${dateFormatter.format(initTime)}.log")
            BotVariables.log.createNewFile()
            return BotVariables.log
        } catch (e: IOException) {
            daemonLogger.error("初始化 Log 文件失败")
        }

        return null
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

    fun createBlankFile(location: File): File {
        if (!location.exists()) location.createNewFile()
        return location
    }

    fun initResourceFile() {
        try {
            daemonLogger.info("正在加载资源文件...")
            val resourcePath = "resources"
            val jarFile = File(Comet.javaClass.protectionDomain.codeSource.location.path)

            if (jarFile.isFile) {
                copyFromJar(jarFile = jarFile.toPath(), target = getResourceFolder().toPath())
            } else { // Run with IDE
                val url: URL = ClassLoader.getSystemResource("/$resourcePath")
                val apps = File(url.toURI())
                for (app in apps.listFiles() ?: return) {
                    copyFolder(app, getResourceFolder())
                }
            }
        } catch (e: IOException) {
            daemonLogger.info("加载资源文件失败, 部分需要图片资源的功能将无法使用")
            daemonLogger.warningS("Cannot copy resources files", e)
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
     *
     * 注意: 该方法可能与部分 JDK 不兼容! (已知 Oracle JRE 8 @Windows Server 2019 会报错)
     */
    private fun copyFromJar(jarFile: Path, source: String = "resources", target: Path) {
        val fileSystem = FileSystems.newFileSystem(jarFile, null)
        val jarPath: Path = fileSystem.getPath(source)

        Files.walkFileTree(jarPath, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                try {
                    val relative = jarPath.relativize(dir)
                    val currentTarget = target.resolve(relative.toString())
                    if (!currentTarget.toFile().exists()) {
                        Files.createDirectories(currentTarget)
                        daemonLogger.debugS("Created directory $currentTarget successfully")
                    }
                } catch (e: IOException) {
                    daemonLogger.warningS("Can't create dir ${dir.fileName} from jar", e)
                } finally {
                    return FileVisitResult.CONTINUE
                }
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                try {
                    val relative = jarPath.relativize(file)
                    val copyTarget = target.resolve(relative.toString())
                    try {
                        if (!copyTarget.toFile().exists()) {
                            Files.copy(file, copyTarget, StandardCopyOption.REPLACE_EXISTING)
                            daemonLogger.debugS("Copied file ${file.fileName}")
                        }
                    } catch (e: UnsupportedOperationException) {
                        Files.copy(file, copyTarget, StandardCopyOption.REPLACE_EXISTING)
                    }
                } catch (e: IOException) {
                    daemonLogger.warningS("Can't copy ${file.fileName} from jar", e)
                } finally {
                    return FileVisitResult.CONTINUE
                }
            }
        })
    }

    fun isSameFile(file: File, toCompare: File): Boolean {
        if (!file.exists() || !toCompare.exists()) return false
        return file.getMD5() == toCompare.getMD5()
    }
}