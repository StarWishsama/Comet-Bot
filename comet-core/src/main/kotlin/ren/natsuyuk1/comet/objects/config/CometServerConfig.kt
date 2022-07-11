package ren.natsuyuk1.comet.objects.config

import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

class CometServerConfig : PersistDataFile<CometServerConfig.Data>(
    File(configDirectory, "server_config.json"),
    Data()
) {
    @kotlinx.serialization.Serializable
    data class Data(
        val port: Int = 7890,
        val githubSecret: String = ""
    )
}
