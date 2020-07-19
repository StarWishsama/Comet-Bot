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
fun File.writeJson(context: Any) {
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

object FileUtil {
    fun getCacheFolder(): File {
        val cacheFolder = File(Comet.filePath.path + File.separator + "cache")
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs()
        }
        return cacheFolder
    }

    fun initLog() {
        try {
            val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
            val initTime = LocalDateTime.now()
            val parent = File(getJarLocation() + File.separator + "logs")
            if (!parent.exists()) {
                parent.mkdirs()
            }
            Comet.log = File(parent, "log-${dateFormatter.format(initTime)}.log")
            Comet.log.createNewFile()
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
}