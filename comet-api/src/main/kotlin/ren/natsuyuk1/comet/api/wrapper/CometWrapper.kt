package ren.natsuyuk1.comet.api.wrapper

import org.jline.reader.LineReader
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol

/**
 * 代表一个 [CometWrapper]
 *
 * 使用 SPI 提供创建对应 [Comet] 实例的桥梁
 */
interface CometWrapper {
    /**
     * 创建一个 [Comet] 实例
     *
     * @param config [Comet] 的配置文件 [CometConfig]
     * @param protocol 在 QQ 平台登录 [Comet] 时使用的协议
     * @param classLoader 加载此 Wrapper 的 [ClassLoader]
     * @param reader [LineReader]
     *
     * @return Comet 实例
     */
    suspend fun createInstance(
        config: CometConfig,
        protocol: MiraiLoginProtocol?,
        classLoader: ClassLoader,
        reader: LineReader
    ): Comet

    /**
     * 此 [CometWrapper] 对应的登录平台
     */
    fun platform(): LoginPlatform
}
