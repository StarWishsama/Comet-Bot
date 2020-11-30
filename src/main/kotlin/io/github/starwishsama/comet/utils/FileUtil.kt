package io.github.starwishsama.comet.utils

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import cn.hutool.core.net.URLDecoder
import cn.hutool.crypto.SecureUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import io.github.starwishsama.comet.utils.StringUtil.getLastingTime
import net.mamoe.mirai.utils.asHumanReadable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.jar.JarFile
import kotlin.time.ExperimentalTime


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

@Synchronized
fun File.getContext(): String = FileReader.create(this, Charsets.UTF_8).readString()

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

fun File.getChildFolder(folderName: String, createIfNotExists: Boolean = true): File {
    val childFolder = File(this, folderName)

    if (!createIfNotExists) return childFolder

    if (!childFolder.exists()) {
        childFolder.mkdirs()
    }
    return childFolder
}

/**
 * 检测 [File] 为文件夹时是否为空
 *
 * 注意：如果 [File] 不是文件夹, 会返回 false
 */
fun File.isEmpty(): Boolean = this.filesCount() != -1 || this.filesCount() > 0

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

    fun createErrorReportFile(reason: String = "发生了一个错误", type: String, t: Throwable, content: String, message: String) {
        val fileName = "$type-${dateFormatter.format(LocalDateTime.now())}.txt"
        val location = File(getErrorReportFolder(), fileName)
        if (location.exists()) return

        location.createNewFile()

        val report = "发生了一个错误:\n${getBeautyStackTrace(t)}\n可能有用的信息: $message\n\n原始获取内容:\n$content"
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

    @Suppress("SpellCheckingInspection")
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

    @OptIn(ExperimentalTime::class)
    fun initResourceFile() {
        val startTime = LocalDateTime.now()
        try {
            daemonLogger.info("正在加载资源文件...")
            val resourcePath = "resources"
            val jarFile = File(Comet.javaClass.protectionDomain.codeSource.location.path)

            if (jarFile.isFile) {
                copyFromJar(jarFile, resourcePath)
            } else { // Run with IDE
                val url: URL = ClassLoader.getSystemResource("/$resourcePath")
                val apps = File(url.toURI())
                for (app in apps.listFiles() ?: return) {
                    copyFolder(app, getResourceFolder())
                }
            }
        } catch (e: Exception) {
            daemonLogger.info("加载资源文件失败, 部分需要图片资源的功能将无法使用")
            daemonLogger.warningS("Cannot copy resources files", e)
        } finally {
            daemonLogger.info("尝试加载资源文件用时 ${startTime.getLastingTime().asHumanReadable}")
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
     * 使用 [JarFile]
     *
     * @param jarFile jar 文件路径
     * @param resourcePath 取出文件的路径
     */
    @Suppress("SameParameterValue")
    private fun copyFromJar(jarFile: File, resourcePath: String) {
        var isInsideResource = false

        JarFile(jarFile).use { jar ->
            val entries = jar.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name

                if (entryName.startsWith("$resourcePath/")) {
                    if (!isInsideResource) isInsideResource = true

                    val actualName = entryName.replace("${resourcePath}/", "").removeSuffix("/")

                    if (actualName.isNotEmpty()) {
                        if (entry.isDirectory && entryName != "$resourcePath/") {
                            File(getResourceFolder(), "$actualName/").mkdirs()
                        } else {
                           val f = File(getResourceFolder(), actualName)

                            if (!f.exists()) {
                                f.createNewFile()
                            }

                            if (f.lastModified().toLocalDateTime() >
                                    entry.lastModifiedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()) {
                                FileOutputStream(f).use { fos ->
                                    val byteArray = ByteArray(1024)
                                    var i: Int
                                    javaClass.classLoader.getResourceAsStream(entryName).use { fis ->
                                        if (fis != null) {
                                            //While the input stream has bytes
                                            while (fis.read(byteArray).also { i = it } > 0) {
                                                fos.write(byteArray, 0, i)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (isInsideResource) {
                    break
                }
            }
        }
    }
}