package io.github.starwishsama.comet.objects

import io.github.starwishsama.comet.enums.MusicApiType
import io.github.starwishsama.comet.enums.PicSearchApiType
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.yamlkt.Comment
import java.net.Proxy

@Serializable
data class CometConfig(
        @Comment("机器人的账号")
        var botId: Long = 0,

        @Comment("机器人的密码")
        var botPassword: String = "",

        @Comment("自动保存数据周期, 单位分钟")
        val autoSaveTime: Long = 60,

        @Comment("RCON 服务器地址, 不使用 RCON 功能可不填")
        var rConUrl: String? = null,

        @Comment("RCON 服务器端口, 不使用 RCON 功能可不填")
        var rConPort: Int = 0,

        @Comment("RCON 服务器连接密码, 不使用 RCON 功能可不填")
        var rConPassword: String? = null,

        @Comment("执行所有机器人命令的全局冷却时间, 单位秒")
        val coolDownTime: Int = 5,

        @Comment("机器人发送消息需要屏蔽的词汇")
        val filterWords: MutableList<String> = mutableListOf(),

        @Comment("点歌 API 类型, 可选的有 QQ, NETEASE")
        var musicApi: MusicApiType = MusicApiType.QQ,

        @Comment("命令前缀")
        val commandPrefix: MutableList<String> = mutableListOf(".", "。", "#", "!", "/", "！"),

        @Deprecated("在未来移动到 ApiConfig")
        @Comment("哔哩哔哩账号用户名, 调用需鉴权API功能 (如 /bili info) 时必填")
        var biliUserName: String? = null,

        @Deprecated("在未来移动到 ApiConfig")
        @Comment("哔哩哔哩账号密码, 调用需鉴权API功能 (如 /bili info) 时必填")
        var biliPassword: String? = null,

        @Deprecated("在未来移动到 ApiConfig")
        @Comment("SauceNao 以图搜图 APIKey, 不填亦可, 但可搜索次数会减少")
        val sauceNaoApiKey: String? = null,

        @Deprecated("Replaced with r6StatsKey")
        @Comment("彩虹六号玩家数据 API, 目前已停止使用")
        val r6tabKey: String? = null,

        @Deprecated("在未来移动到 ApiConfig")
        @Comment("彩虹六号玩家数据 API, 需要自行在 R6Stats Discord 群中申请\n" +
                "详见: https://discordapp.com/channels/293848587391991836/575341069399752715/741297488543219712")
        val r6StatsKey: String? = null,

        @Deprecated("在未来移动到 ApiConfig")
        @Comment("用于获取 Twitter Token 的 Access Token, 使用 Twitter 推送必填")
        var twitterAccessToken: String? = null,

        @Deprecated("在未来移动到 ApiConfig")
        @Comment("用于获取 Twitter Token 的 Access Secret, 使用 Twitter 推送必填")
        var twitterAccessSecret: String? = null,

        @Deprecated("在未来移动到 ApiConfig")
        @Comment("用于使用 Twitter API 的 Token, 无需填写会自动获取")
        var twitterToken: String? = null,

        @Comment("是否启用代理, 启用后以下两项才可使用")
        var proxySwitch: Boolean = false,

        @Comment("本地代理服务器地址, 目前仅支持 HTTP 代理")
        var proxyUrl: String = "",

        @Comment("本地代理服务器端口, 目前仅支持 HTTP 代理")
        var proxyPort: Int = 0,

        @Comment("本地代理类型, 支持 HTTP 和 SOCKS")
        var proxyType: Proxy.Type = Proxy.Type.HTTP,

        @Comment("Mirai 心跳周期, 单位分钟, 过长会导致被服务器断开连接")
        var heartBeatPeriod: Long = 1,

        @Comment("Youtube APIKey, 使用 Youtube 推送必填\n申请请见 https://developers.google.com/youtube/v3/getting-started")
        var youtubeApiKey: String = "",

        @Comment("推文推送时候是否发送小图而不是原图")
        var smallImageMode: Boolean = true,

        @Comment("以图搜图 API 类型, 请使用 /ps source 命令修改")
        var pictureSearchApi: PicSearchApiType = PicSearchApiType.SAUCENAO,

        @Deprecated("在未来使用 PusherConfig 代替")
        @Comment("查询哔哩哔哩用户动态间隔时间")
        val biliInterval: Long = 2,

        @Deprecated("在未来使用 PusherConfig 代替")
        @Comment("查询 Twitter 用户动态间隔时间")
        val twitterInterval: Long = 4,

        @Comment("明日方舟抽卡模拟器是否使用图片")
        var arkDrawUseImage: Boolean = false,

        @Comment("调试模式, 打开后会有更多 Log 并启用调试功能")
        var debugMode: Boolean = false,

        @Comment("WebDriver 使用的浏览器, 留空为关闭")
        val webDriverName: String = "",

        @Comment("WebDriver Remote 地址, 留空为关闭")
        val remoteWebDriver: String = "",

        @Comment("机器人使用的登录协议, 可选的有 ANDROID_PHONE, ANDROID_PAD 和 ANDROID_WATCH")
        val botProtocol: BotConfiguration.MiraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PAD,

        @Comment("自动清理过时文件间隔 (如 Log, 错误报告等), 单位为天. 设为小于 0 的数字以关闭")
        val autoCleanDuration: Int = 15
)