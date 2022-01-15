/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import cn.hutool.core.net.URLDecoder
import cn.hutool.crypto.SecureUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.CometPlugin
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.utils.StringUtil.getLastingTime
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import io.github.starwishsama.comet.utils.StringUtil.toFriendly
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.time.ExperimentalTime

@Synchronized
fun File.writeClassToJson(context: Any, mapper: ObjectMapper = CometVariables.mapper) {
    daemonLogger.debug("正在尝试写入文件: ${this.absolutePath}")
    FileWriter.create(this).getWriter(false).use {
        mapper.writeValue(it, context)
    }
}

@Synchronized
fun File.writeString(
    context: String,
    autoWrap: Boolean = true,
    isAppend: Boolean = false,
    newIfNotExists: Boolean = true
) {
    daemonLogger.debug("正在尝试写入文件: ${this.absolutePath}")

    if (newIfNotExists) {
        createNewFile()
    }

    if (isAppend) {
        FileWriter.create(this).write(getContext() + if (autoWrap) context + "\n" else context, isAppend)
    } else {
        FileWriter.create(this).write(if (autoWrap) context + "\n" else context, isAppend)
    }
}

@Synchronized
fun File.getContext(): String {
    return FileReader.create(this, Charsets.UTF_8).readString()
}

@Suppress("unused")
fun File.getMD5(): String {
    return SecureUtil.md5(this)
}

/**
 * 直接将文件内容 (json) 序列化为指定的类
 *
 * @return T
 */
inline fun <reified T : Any> File.parseAsClass(customParser: ObjectMapper = CometVariables.mapper): T {
    require(exists()) { "$path 不存在" }
    return customParser.readValue(getContext())
}

fun File.getChildFolder(folderName: String, createIfNotExists: Boolean = true): File {
    require(exists()) { "$path 不存在" }

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
@Suppress("unused")
fun File.folderIsEmpty(): Boolean {
    require(exists()) { "$name 不存在" }
    return filesCount() < 1
}

fun File.filesCount(): Int {
    require(exists()) { "$name 不存在" }

    if (!isDirectory) return -1

    val files = listFiles() ?: return -1

    return files.size
}

fun File.copyAndRename(name: String) {
    require(exists()) { "$name 不存在" }

    if (isDirectory) return

    val backup = File(parent, name)
    backup.createNewFile()
    Files.copy(toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING)
}

fun File.createBackupFile() {
    require(exists()) { "$name 不存在" }

    if (isDirectory) return

    copyAndRename("$name.backup")
}

object FileUtil {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    fun getChildFolder(childName: String): File = CometVariables.filePath.getChildFolder(childName)

    fun getCacheFolder(): File = getChildFolder("cache")

    fun getResourceFolder(): File = getChildFolder("res")

    fun getErrorReportFolder(): File = getChildFolder("error-reports")

    fun createErrorReportFile(
        reason: String = "发生了一个错误",
        type: String,
        t: Throwable?,
        content: String,
        message: String
    ) {
        val fileName = "$type-${dateFormatter.format(LocalDateTime.now())}.txt"
        val location = File(getErrorReportFolder(), fileName)
        if (location.exists()) return

        location.createNewFile()

        val report =
            "发生了一个错误:\n${if (t != null) getBeautyStackTrace(t) else "无报错堆栈"}\n可能有用的信息: ${message.limitStringSize(150)}\nComet 版本: ${BuildConfig.version}\n\n原始获取内容:\n$content"
        location.writeString(report)
        daemonLogger.warning("$reason, 错误报告已生成! 保存在 ${location.path}")
        daemonLogger.warning("你可以将其反馈到 https://github.com/StarWishsama/Comet-Bot/issues")
    }

    /**
     * 获取当前 Log 文件
     *
     * @return log 文件位置
     */
    fun getLogLocation(customPrefix: String = "log"): File {
        val initTime = LocalDateTime.now()
        val parent = getChildFolder("logs")
        return File(parent, "$customPrefix-${dateFormatter.format(initTime)}.log").also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }
    }

    /**
     * 获取当前 jar 文件绝对位置
     */
    @Suppress("SpellCheckingInspection")
    fun getJarLocation(): File {
        var path: String = Comet::class.java.protectionDomain.codeSource.location.path
        if (System.getProperty("os.name").lowercase().contains("dows")) {
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

    /**
     * 在指定位置创建新的空文件
     *
     * @param location 位置
     *
     * @return 返回对应位置的 [File]
     */
    fun createBlankFile(location: File): File {
        if (!location.exists()) location.createNewFile()
        return location
    }

    fun createTempFile(content: Any, deleteOnExit: Boolean) {
        val f = File(getCacheFolder(), "temp${System.currentTimeMillis()}.tmp")
        if (deleteOnExit) {
            f.deleteOnExit()
        }
        f.createNewFile()
        f.writeText(content.toString())
    }

    /**
     * 初始化资源文件
     */
    @OptIn(ExperimentalTime::class)
    fun initResourceFile() {
        val startTime = LocalDateTime.now()
        try {
            daemonLogger.info("正在加载内嵌资源文件...")
            val resourcePath = "resources"
            val jarFile = File(CometPlugin.javaClass.protectionDomain.codeSource.location.path)

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
            daemonLogger.info("加载资源文件失败, 部分需要资源的功能将无法使用")
            daemonLogger.debug("Cannot copy resources files", e)
        } finally {
            daemonLogger.info("尝试加载资源文件用时 ${startTime.getLastingTime().toFriendly(TimeUnit.SECONDS)}")
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
        JarFile(jarFile).use { jar ->
            val entries = jar.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name

                if (entryName.startsWith("$resourcePath/")) {
                    val actualName = entryName.replace("${resourcePath}/", "").removeSuffix("/")

                    processFileInJar(jar, entry, actualName, "resources")
                } else {
                    continue
                }
            }
        }
    }

    /**
     * 处理 jar 中的文件
     *
     * @param entry jar
     * @param fileName 文件名
     * @param resourcePath 资源文件储存文件夹
     */
    @Suppress("SameParameterValue")
    private fun processFileInJar(jar: JarFile, entry: JarEntry, fileName: String, resourcePath: String) {
        var counter = 0

        if (fileName.isEmpty()) {
            return
        }

        val entryName = entry.name

        if (entry.isDirectory && entryName != "$resourcePath/") {
            File(getResourceFolder(), "$fileName/").mkdirs()
        } else {
            val current = File(getResourceFolder(), fileName)

            if (!current.exists() || current.lastModified() > entry.lastModifiedTime.toMillis()) {
                if (!current.exists()) {
                    current.createNewFile()
                }

                jar.getInputStream(entry).use {
                    Files.copy(it, current.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    counter++
                }
            }
        }

        daemonLogger.debug("在 ${entry.name} 下已处理 $counter 个数据文件.")
    }
}