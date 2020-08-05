package io.github.starwishsama.comet.objects

import io.github.starwishsama.comet.enums.MusicApi
import io.github.starwishsama.comet.enums.PicSearchApi
import kotlinx.serialization.Serializable

//import net.mamoe.yamlkt.Comment

@Serializable
data class Config(
    //@Comment("机器人的账号")
    var botId: Long = 0,

    //@Comment("机器人的密码")
    var botPassword: String = "",

    //@Comment("自动保存数据周期, 单位分钟")
    var autoSaveTime: Long = 60,

    //@Comment("RCON 服务器地址, 不使用 RCON 功能可不填")
    var rConUrl: String? = null,

    //@Comment("RCON 服务器端口, 不使用 RCON 功能可不填")
    var rConPort: Int = 0,

    //@Comment("RCON 服务器连接密码, 不使用 RCON 功能可不填")
    var rConPassword: String? = null,

    //@Comment("执行所有机器人命令的全局冷却时间, 单位秒")
    var coolDownTime: Int = 5,

    //@Comment("机器人发送消息需要屏蔽的字词")
    var filterWords: MutableList<String> = mutableListOf(),

    //@Comment("点歌 API 类型")
    var musicApi: MusicApi = MusicApi.QQ,

    //@Comment("命令前缀")
    var commandPrefix: MutableList<String> = mutableListOf(".", "。", "#", "!", "/"),

    //@Comment("BiliBili 账号, 使用开播提醒时必填")
    var biliUserName: String? = null,

    //@Comment("BiliBili 账号密码, 使用开播提醒时必填")
    var biliPassword: String? = null,

    //@Comment("机器人主人QQ号, 对应的用户将自动获得 OWNER 权限组")
    var ownerId: Long = 0L,

    //@Comment("SauceNao 以图搜图 APIKey, 不填亦可, 但可搜索次数会减少")
    var sauceNaoApiKey: String? = null,

    //@Comment("彩虹六号玩家数据 API, 需要自行向 r6tab 作者申请")
    var r6tabKey: String? = null,

    //@Comment("用于获取 Twitter Token 的 Access Token")
    var twitterAccessToken: String? = null,

    //@Comment("用于获取 Twitter Token 的 Access Secret")
    var twitterAccessSecret: String? = null,

    //@Comment("用于使用 Twitter Developer API 的 Token")
    var twitterToken: String? = null,

    //@Comment("代理服务器地址, 目前仅支持 HTTP 代理")
    var proxyUrl: String = "",

    //@Comment("代理服务器端口, 目前仅支持 HTTP 代理")
    var proxyPort: Int = 0,

    //@Comment("Mirai 心跳周期, 单位分钟, 过长会导致被服务器断开连接")
    var heartBeatPeriod: Int = 2,

    //@Comment("Youtube APIKey, 申请请见 https://developers.google.com/youtube/v3/getting-started")
    var youtubeApiKey: String = "",

    //@Comment("推文推送时候是否发送小图而不是原图")
    var smallImageMode: Boolean = true,

    //@Comment("以图搜图 API 类型, 请使用 /ps source 命令修改")
    var pictureSearchApi: PicSearchApi = PicSearchApi.SAUCENAO
)