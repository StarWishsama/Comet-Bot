package ren.natsuyuk1.comet.objects.config

import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object CometServerConfig : PersistDataFile<CometServerConfig.Data>(
    File(configDirectory, "server_config.yml"),
    Data(),
    Yaml(),
    readOnly = true
) {
    @kotlinx.serialization.Serializable
    data class Data(
        @Comment("服务器开关")
        val switch: Boolean = false,
        @Comment("服务器端口")
        val port: Int = 1145,
        @Comment("服务器对外展示的域名, 在订阅时展示, 如 https://example.com")
        val serverName: String = ""
    )
}
