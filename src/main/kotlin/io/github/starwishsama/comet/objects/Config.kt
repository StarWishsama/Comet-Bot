package io.github.starwishsama.comet.objects

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.enums.MusicApi
import java.util.*

open class Config {
    @SerializedName("botId")
    var botId: Long = 0

    @SerializedName("botPassword")
    var botPassword: String = ""

    @SerializedName("auto_save_config_time")
    var autoSaveTime: Long = 60

    @SerializedName("rcon_url")
    var rConUrl: String? = null

    @SerializedName("rcon_port")
    var rConPort: Int = 0

    @SerializedName("rcon_password")
    var rConPassword: String? = null

    @SerializedName("netease_api")
    var netEaseApi: String = ""

    @SerializedName("cool_down_time")
    var coolDownTime: Int = 5

    @SerializedName("filter_words")
    var filterWords : List<String> = mutableListOf()

    @SerializedName("default_music_api")
    var musicApi : MusicApi = MusicApi.QQ

    @SerializedName("command_prefix")
    var commandPrefix: List<String> = mutableListOf(".", "。", "#", "!", "/")

    @SerializedName("bili_user_name")
    var biliUserName: String? = null

    @SerializedName("bili_password")
    var biliPassword: String? = null

    @SerializedName("owner_id")
    var ownerId: Long = 0L

    @SerializedName("saucenao_api_key")
    var saucenaoApiKey: String? = null

    @SerializedName("r6tab_apikey")
    var r6tabKey: String? = null

    @Deprecated("Soon will replace by PerGroupSetting")
    @SerializedName("universal_subs")
    var subList = LinkedList<Long>()

    @Deprecated("Soon will replace by PerGroupSetting")
    @SerializedName("push_groups")
    var pushGroups : List<Long> = mutableListOf()

    @SerializedName("check_delay")
    var checkDelay: Long = 1

    @SerializedName("twitter_token")
    var twitterToken: String? = null

    @SerializedName("twitter_secret")
    var twitterSecret: String? = null

    @Deprecated("Soon will replace by PerGroupSetting")
    @SerializedName("twitter_subs")
    var twitterSubs: List<String> = LinkedList()

    @Deprecated("Soon will replace by PerGroupSetting")
    @SerializedName("tweet_push_groups")
    var tweetPushGroups: List<Long> = LinkedList()

    @SerializedName("proxy_url")
    var proxyUrl: String = ""

    @SerializedName("proxy_port")
    var proxyPort: Int = 0

    @SerializedName("mirai_heartbeat_period")
    var heartBeatPeriod: Long = 2

    @SerializedName("youtube_api_key")
    var youtubeApiKey: String = ""

    @SerializedName("small_image_mode")
    var smallImageMode: Boolean = true
}