package ren.natsuyuk1.comet.cli

import mu.KotlinLogging
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

private val logger = KotlinLogging.logger {}

object WrapperLoader {
    private val modules = resolveDirectory("./modules")
    private lateinit var serviceLoader: ServiceLoader<CometWrapper>
    var classLoader: ClassLoader = ClassLoader.getPlatformClassLoader()
        private set

    suspend fun load() {
        modules.touch()

        val possibleModules = (modules.listFiles() ?: emptyArray<File>()).filter { it.name.endsWith(".jar") }

        val urls = Array<URL>(possibleModules.size) { possibleModules[it].toURI().toURL() }

        classLoader = URLClassLoader.newInstance(urls)

        serviceLoader = ServiceLoader.load(CometWrapper::class.java, classLoader)
    }

    fun getService(platform: LoginPlatform): CometWrapper? {
        if (!::serviceLoader.isInitialized) {
            return null
        }

        return serviceLoader.find { it.platform() == platform }
    }
}
