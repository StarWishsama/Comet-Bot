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
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.jar.JarFile
import kotlin.io.path.absolute
import kotlin.io.path.pathString

val globalDirectory by lazy {
    File(System.getProperty("user.dir"))
}

fun resolveDirectory(path: String) = File(globalDirectory, "/$path")
fun resolveResourceDirectory(path: String) = File(resourceDirectory, path)

val configDirectory by lazy { resolveDirectory("./config") }

val resourceDirectory by lazy { resolveDirectory("./resources") }

val messageWrapperDirectory by lazy { resolveDirectory("./msgwrapper") }

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
        input.use {
            target.outputStream().use { output ->
                input.copyTo(output)
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
    target: File,
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
