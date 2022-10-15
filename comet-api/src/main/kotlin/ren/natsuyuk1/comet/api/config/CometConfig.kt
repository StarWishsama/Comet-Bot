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

object CometGlobalConfig : PersistDataFile<CometGlobalConfig.Data>(
    File(configDirectory, "config.yml"),
    Data(),
    Yaml,
    readOnly = true
) {
    @Serializable
    data class Data(
        @Comment("Comet 消息前缀")
        val prefix: String = "Comet > ",
        /**
         * 自动保存数据的周期, 单位为分钟
         */
        @Comment("自动保存数据的周期, 单位为分钟")
        var dataSaveDuration: Long = 60,

        /**
         * 进行网络请求时的 User-Agent, 进行部分特殊请求时不使用该 User-Agent
         */
        @Comment("进行网络请求时的 User-Agent, 进行部分特殊请求时不使用该 User-Agent")
        /* ktlint-disable max-line-length */
        var useragent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.124 Safari/537.36 Edg/102.0.1245.41",
        /* ktlint-enable max-line-length */

        @Comment("命令前缀")
        val commandPrefix: List<String> = mutableListOf("/", "!"),

        @Comment("命令执行冷却, 单位秒")
        val commandCoolDown: Int = 3,

        @Comment("全局限速间隔, 单位分钟")
        val globalRateLimitInterval: Int = 1,

        @Comment("全局限速时间内最高可发送消息数")
        val globalRateLimitMessageSize: Int = 60,

        @Comment("哔哩哔哩 Cookie, 用于用户搜索等需要 Cookie 的 API")
        val biliCookie: String = "",

        @Comment("Apex Legends API Token, 用于搜索 Apex 玩家信息")
        val apexLegendToken: String = "",

        @Comment("SauceNao API Token, 用于 SauceNao 平台的以图搜图")
        val sauceNaoToken: String = "",
    )
}

data class CometConfig(val id: Long, val password: String, val platform: LoginPlatform)
