/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.config

import io.github.starwishsama.comet.enums.MusicApiType
import io.github.starwishsama.comet.enums.PicSearchApiType
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.yamlkt.Comment
import java.net.Proxy

@Serializable
data class CometConfig(
    /**
     * Mirai 核心相关
     */
    @Comment("机器人的 QQ 账号")
    var botId: Long = 0,

    @Comment("机器人的 QQ 密码")
    var botPassword: String = "",

    @Comment("Mirai 使用的登录协议, 可选的有 ANDROID_PHONE (推荐), ANDROID_PAD (可同时在线) 和 ANDROID_WATCH (支持功能少)")
    val botProtocol: BotConfiguration.MiraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE,

    @Comment("Mirai 的心跳策略, 可选的有 STAT_HB(推荐), REGISTER, NONE\n仅当无法正常工作时才修改此项!")
    val heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.HeartbeatStrategy.STAT_HB,

    @Comment("自动保存数据周期, 单位分钟")
    val autoSaveTime: Long = 60,

    @Comment("执行所有机器人命令的全局冷却时间, 单位秒")
    val coolDownTime: Int = 5,

    @Comment("自动清理过时文件间隔 (如 Log, 错误报告等), 单位为天. 设为小于 0 的数字以关闭")
    val autoCleanDuration: Int = 15,

    @Comment("调度池最大容量, 默认为 30")
    val maxPoolSize: Int = 30,

    @Comment("明日方舟抽卡模拟器是否使用图片")
    var arkDrawUseImage: Boolean = false,

    @Comment("调试模式, 打开后会有更多 Log 并启用调试功能")
    var debugMode: Boolean = false,

    @Comment("Webhook 功能开关")
    val webHookSwitch: Boolean = false,

    @Comment("RCON 服务器地址, 不使用 RCON 功能可不填")
    var rConUrl: String? = null,

    @Comment("RCON 服务器端口, 不使用 RCON 功能可不填")
    var rConPort: Int = 0,

    @Comment("RCON 服务器连接密码, 不使用 RCON 功能可不填")
    var rConPassword: String? = null,

    @Comment("机器人发送消息需要屏蔽的词汇")
    val filterWords: MutableList<String> = mutableListOf(),

    @Comment("点歌 API 类型, 可选的有 QQ, NETEASE")
    var musicApi: MusicApiType = MusicApiType.QQ,

    @Comment("命令前缀")
    val commandPrefix: MutableList<String> = mutableListOf(".", "。", "#", "!", "/", "！"),

    @Comment("是否启用代理, 启用后以下两项才可使用")
    var proxySwitch: Boolean = false,

    @Comment("本地代理服务器地址")
    var proxyUrl: String = "",

    @Comment("本地代理服务器端口")
    var proxyPort: Int = 0,

    @Comment("本地代理类型, 选填项为 HTTP 和 SOCKS\nSOCKS 选项支持 v4/v5")
    var proxyType: Proxy.Type = Proxy.Type.HTTP,

    @Comment("以图搜图 API 类型, 请使用 /ps source 命令修改")
    var pictureSearchApi: PicSearchApiType = PicSearchApiType.SAUCENAO,

    @Comment("Webhook 推送服务器端口")
    val webHookPort: Int = 6789,

    @Comment("Webhook 服务器链接, 将会在订阅成功后展示.\n不必写服务器 IP 地址, Comet 只会解析地址后路由路径\n如默认的地址在 Github 中需填写为 http://example.address.com/payload")
    val webHookAddress: String = "http://example.address.com/payload",

    @Comment("Webhook Secret, 为确保服务器安全, 推荐填写")
    val webHookSecret: String = "",
)