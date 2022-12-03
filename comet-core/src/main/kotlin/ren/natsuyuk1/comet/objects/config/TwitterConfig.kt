package ren.natsuyuk1.comet.objects.config

import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object TwitterConfig : PersistDataFile<TwitterConfig.Data>(
    File(configDirectory, "twitter.yml"),
    Data.serializer(),
    Data(),
    Yaml(),
    readOnly = true
) {
    @kotlinx.serialization.Serializable
    data class Data(
        @Comment("Twitter API v2.0 Bearer Token, 以 AAA 开头")
        val token: String = "",
    )
}
