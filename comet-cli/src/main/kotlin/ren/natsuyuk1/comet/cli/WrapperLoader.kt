package ren.natsuyuk1.comet.cli

import mu.KotlinLogging
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarFile

private val logger = KotlinLogging.logger {}

object WrapperLoader {
    private val modules = resolveDirectory("./modules")
    private val classes = mutableMapOf<String, Class<*>>()

    suspend fun autoDiscovery() {
        modules.touch()

        val possibleModules = (modules.listFiles() ?: emptyArray<File>()).filter { it.name.endsWith(".jar") }

        if (possibleModules.isEmpty()) {
            return
        }

        val urls = Array<URL>(possibleModules.size) { possibleModules[it].toURI().toURL() }

        val cl = URLClassLoader(urls, CometTerminal::class.java.classLoader)

        possibleModules.forEach { loc ->
            val jarFile = JarFile(loc)
            val jarEntries = jarFile.entries()

            while (jarEntries.hasMoreElements()) {
                val je: JarEntry = jarEntries.nextElement()

                if (je.isDirectory || !je.name.endsWith(".class")) {
                    continue
                }

                val className = je.name.substring(0, je.name.length - 6).replace('/', '.')

                if (className.startsWith("META-INF") || className.startsWith("kotlin.")) {
                    continue
                }

                if (className.startsWith("ch.qos.logback")
                    || className.startsWith("okhttp")
                    || className.startsWith("io.netty")
                    || className.startsWith("org.apache.logging.log4j")
                    || className.startsWith("org.bouncycastle")
                    || className.startsWith("cn.hutool")
                ) {
                    continue
                }

                try {
                    val clazz = Class.forName(className, true, cl)
                    classes[className] = clazz

                    if (className.startsWith("ren.natsuyuk1.comet")) {
                        logger.info { "Loading $className" }
                    }
                } catch (e: Throwable) {
                    logger.warn(e) { "无法加载类 $className" }
                }
            }
        }
    }
}
