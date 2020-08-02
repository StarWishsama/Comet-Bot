package io.github.starwishsama.comet.objects

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.enums.MusicApi
import io.github.starwishsama.comet.enums.PicSearchApi

data class Config(
    @SerializedName("botId")
    var botId: Long = 0,

    @SerializedName("botPassword")
    var botPassword: String = "",

    @SerializedName("auto_save_config_time")
    var autoSaveTime: Long = 60,

    @SerializedName("rcon_url")
    var rConUrl: String? = null,

    @SerializedName("rcon_port")
    var rConPort: Int = 0,

    @SerializedName("rcon_password")
    var rConPassword: String? = null,

    @SerializedName("netease_api")
    var netEaseApi: String = "",

    @SerializedName("cool_down_time")
    var coolDownTime: Int = 5,

    @SerializedName("filter_words")
    var filterWords: List<String> = mutableListOf(),

    @SerializedName("default_music_api")
    var musicApi: MusicApi = MusicApi.QQ,

    @SerializedName("command_prefix")
    var commandPrefix: List<String> = mutableListOf(".", "。", "#", "!", "/"),

    @SerializedName("bili_user_name")
    var biliUserName: String? = null,

    @SerializedName("bili_password")
    var biliPassword: String? = null,

    @SerializedName("owner_id")
    var ownerId: Long = 0L,

    @SerializedName("saucenao_api_key")
    var saucenaoApiKey: String? = null,

    @SerializedName("r6tab_apikey")
    var r6tabKey: String? = null,

    @SerializedName("twitter_token")
    var twitterToken: String? = null,

    @SerializedName("twitter_secret")
    var twitterSecret: String? = null,

    @SerializedName("proxy_url")
    var proxyUrl: String = "",

    @SerializedName("proxy_port")
    var proxyPort: Int = 0,

    @SerializedName("mirai_heartbeat_period")
    var heartBeatPeriod: Long = 2,

    @SerializedName("youtube_api_key")
    var youtubeApiKey: String = "",
    @SerializedName("small_image_mode")
    var smallImageMode: Boolean = true,

    @SerializedName("picture_search_api")
    var pictureSearchApi: PicSearchApi = PicSearchApi.SAUCENAO
)