package ren.natsuyuk1.comet.cli.wrapper

import mu.KotlinLogging
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper
import ren.natsuyuk1.comet.cli.CometTerminal
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

private val logger = KotlinLogging.logger {}

object WrapperLoader {
    private val modules = resolveDirectory("/modules/")
    private lateinit var serviceLoader: ServiceLoader<CometWrapper>
    var wrapperClassLoader: ClassLoader = ClassLoader.getPlatformClassLoader()
        private set

    suspend fun load() {
        modules.touch()

        val possibleModules = (modules.listFiles() ?: emptyArray<File>()).filter { it.name.endsWith(".jar") }

        val urls = Array<URL>(possibleModules.size) { possibleModules[it].toURI().toURL() }

        wrapperClassLoader = URLClassLoader.newInstance(urls, CometTerminal::class.java.classLoader)

        serviceLoader = ServiceLoader.load(CometWrapper::class.java, wrapperClassLoader)

        val serviceCount = serviceLoader.count()

        if (serviceCount == 0) {
            logger.warn { "未检测到任何 Comet Wrapper, Comet 将无法正常工作!" }
        } else {
            logger.info { "已加载 ${serviceLoader.count()} 个 Comet Wrapper." }
        }
    }

    fun getService(platform: LoginPlatform): CometWrapper? {
        if (!::serviceLoader.isInitialized) {
            return null
        }

        return serviceLoader.find { it.platform() == platform }
    }
}
