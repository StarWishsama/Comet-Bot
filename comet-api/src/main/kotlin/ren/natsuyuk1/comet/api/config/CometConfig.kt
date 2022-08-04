/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.config

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

class CometConfig(val id: Long, val password: String, val platform: LoginPlatform) : PersistDataFile<CometConfig.Data>(
    File(configDirectory, "$id.yml"), Data(), Yaml
) {
    @Serializable
    data class Data(
        /**
         * 自动保存数据的周期, 单位为分钟
         */
        @Comment("自动保存数据的周期, 单位为分钟")
        var dataSaveDuration: Long = 60,

        /**
         * 进行网络请求时的 User-Agent, 进行部分特殊请求时不使用该 User-Agent
         */
        @Comment("进行网络请求时的 User-Agent, 进行部分特殊请求时不使用该 User-Agent")
        var useragent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.124 Safari/537.36 Edg/102.0.1245.41",

        @Comment("命令前缀")
        val commandPrefix: List<String> = mutableListOf("/", "!")
    )
}
