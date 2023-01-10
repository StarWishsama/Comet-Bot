/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.utils.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import okio.buffer
import okio.sink
import okio.source
import ren.natsuyuk1.comet.utils.system.getEnv
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.jar.JarFile
import kotlin.io.path.absolute
import kotlin.io.path.pathString

val workDir by lazy {
    File(
        if (getEnv("comet.workdir").isNullOrBlank()) {
            System.getProperty("user.dir")
        } else getEnv("comet.workdir")
    )
}

fun resolveDirectory(path: String) = File(workDir, path)
fun resolveResourceDirectory(path: String) = File(resourceDirectory, path)

val configDirectory by lazy { resolveDirectory("./config") }

val resourceDirectory by lazy { resolveDirectory("./resources") }

val messageWrapperDirectory by lazy { resolveDirectory("./wrapper-cache") }

val dataDirectory by lazy { resolveDirectory("./data") }

val cacheDirectory by lazy { resolveDirectory("./cache") }

val File.absPath
    get() = toPath().normalize().absolute().pathString

/**
 * Create a file and its parents
 */
suspend fun File.touch(): Boolean = withContext(Dispatchers.IO) {
    parentFile?.mkdirs()
    createNewFile()
}

suspend fun File.writeTextBuffered(text: String) = withContext(Dispatchers.IO) {
    outputStream().bufferedWriter().use { it.write(text) }
}

suspend fun File.readTextBuffered() = withContext(Dispatchers.IO) {
    inputStream().bufferedReader().use(BufferedReader::readText)
}

/**
 * Writes an input stream to a file.
 * @param input The input stream
 * @param target The target file
 * @throws IOException If there is an I/O error
 */
suspend fun writeToFile(input: InputStream, target: File): Unit =
    withContext(Dispatchers.IO) {
        input.source().buffer().use { i ->
            target.sink().buffer().use { o ->
                o.writeAll(i)
            }
        }
    }

/**
 * Copies a resource directory from inside a JAR file to a target directory.
 * @param source The JAR file
 * @param path The path to the directory inside the JAR file
 * @param target The target directory
 * @throws IOException If there is an I/O error
 */
suspend fun copyResourceDirectory(
    source: JarFile,
    path: String,
    target: File
) = withContext(Dispatchers.IO) {
    val newPath = "$path/"
    source.entries().toList().asSequence()
        .filter { it.name.startsWith(newPath) }
        .filterNot { it.isDirectory }
        .forEach {
            val dest = File(target, it.name.substring(newPath.length))
            dest.parentFile?.mkdirs()
            writeToFile(source.getInputStream(it), dest)
        }
}

private const val JAR_URI_PREFIX = "jar:file:"

/**
 * The JAR file containing the given class.
 * @param clazz The class
 * @return The JAR file or null
 * @throws IOException If there is an I/O error
 */
fun jar(clazz: Class<*>): JarFile? {
    val path = "/${clazz.name.replace('.', '/')}.class"
    val url = clazz.getResource(path) ?: return null
    val jar = url.toString()
    val bang = jar.indexOf('!')
    if (jar.startsWith(JAR_URI_PREFIX) && bang != -1) {
        return JarFile(jar.substring(JAR_URI_PREFIX.length, bang))
    }
    return null
}

/**
 * Get file last modified time
 *
 * @return last modified time
 */
fun File.lastModifiedTime(): Instant = Instant.fromEpochMilliseconds(lastModified())

/**
 * Identify a file is blank or not
 *
 * @return is file blank
 */
fun File.isBlank(): Boolean = !exists() || length() == 0L

/**
 * Check specific file type
 *
 * @param type file type to check
 *
 * The param of this method is the string form of the value of a
 * Multipurpose Internet Mail Extension (MIME) content type as
 * defined by <a href="http://www.ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045:
 * Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet
 * Message Bodies</i></a>. The string is guaranteed to be parsable according
 * to the grammar in the RFC.
 *
 * @return is file type equals
 */
fun File.isType(type: String): Boolean = Files.probeContentType(toPath()) == type
