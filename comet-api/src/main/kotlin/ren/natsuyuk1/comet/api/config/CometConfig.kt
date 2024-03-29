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
import org.jline.reader.LineReader
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.json.serializers.UUIDSerializer
import java.io.File
import java.util.*

object CometGlobalConfig : PersistDataFile<CometGlobalConfig.Data>(
    File(configDirectory, "config.yml"),
    Data.serializer(),
    Data(),
    Yaml,
    readOnly = true,
) {
    @Serializable
    data class Data(
        @Serializable(with = UUIDSerializer::class)
        @Comment("Comet WebAPI 访问 Token")
        val accessToken: UUID = UUID.randomUUID(),
        @Comment("Comet 消息前缀")
        val prefix: String = "Comet > ",
        /**
         * 自动保存数据的周期, 单位为分钟
         */
        @Comment("自动保存数据的周期, 单位为分钟")
        val dataSaveDuration: Long = 60,

        /**
         * 进行网络请求时的 User-Agent, 进行部分特殊请求时不使用该 User-Agent
         */
        /* ktlint-disable max-line-length */
        @Comment("进行网络请求时的 User-Agent, 进行部分特殊请求时不使用该 User-Agent")
        val useragent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.124 Safari/537.36 Edg/102.0.1245.41",
        /* ktlint-enable max-line-length */

        @Comment("命令前缀")
        val commandPrefix: List<String> = mutableListOf("/", "!", "."),

        @Comment("命令执行冷却, 单位秒")
        val commandCoolDown: Int = 3,

        @Comment("哔哩哔哩 Cookie, 用于用户搜索等需要 Cookie 的 API")
        val biliCookie: String = "",

        @Comment("Apex Legends API Token, 用于搜索 Apex 玩家信息")
        val apexLegendToken: String = "",

        @Comment("SauceNao API Token, 用于 SauceNao 平台的以图搜图")
        val sauceNaoToken: String = "",

        @Comment("Skiko 绘图工具, 启用后为 Comet 部分命令提供图片结果")
        val skiko: Boolean = true,

        @Comment("Brotli 加解密库, 启用后为 Arcaea 查询功能提供支持")
        val brotli: Boolean = true,
    )
}

data class CometConfig(
    val id: Long,
    val password: String,
    val platform: CometPlatform,
    val protocol: MiraiLoginProtocol? = null,
    val classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
    val reader: LineReader? = null,
)
