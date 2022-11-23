package ren.natsuyuk1.comet.objects.config

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object FeatureConfig : PersistDataFile<FeatureConfig.Data>(
    File(configDirectory, "ipdb.yml"),
    Data(),
    Yaml,
    readOnly = true
) {
    @Serializable
    data class Data(
        @Comment("是否启用 Arcaea 相关功能\n启用后, 会自动下载并加载相关数据文件")
        val arcaea: Boolean = false,
        @Comment("是否启用 Project Sekai 相关功能\n启用后, 会自动下载并加载相关数据文件")
        val projectSekai: Boolean = false,
    )
}
