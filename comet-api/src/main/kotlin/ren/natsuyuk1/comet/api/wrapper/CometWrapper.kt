package ren.natsuyuk1.comet.api.wrapper

import org.jline.reader.LineReader
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometStartupData
import ren.natsuyuk1.comet.api.platform.LoginPlatform

/**
 * 代表一个 [CometWrapper]
 *
 * 使用 SPI 提供创建对应 [Comet] 实例的桥梁
 */
interface CometWrapper {
    /**
     * 创建一个 [Comet] 实例
     *
     * @param config [Comet] 的配置文件 [CometStartupData]
     * @param classLoader 加载此 Wrapper 的 [ClassLoader]
     * @param reader [LineReader]
     *
     * @return Comet 实例
     */
    suspend fun createInstance(
        config: CometStartupData,
        classLoader: ClassLoader,
        reader: LineReader
    ): Comet

    /**
     * 此 [CometWrapper] 对应的登录平台
     */
    fun platform(): LoginPlatform

    /**
     * 此 [CometWrapper] 对应登录平台的库相关信息
     */
    fun libInfo(): String
}
