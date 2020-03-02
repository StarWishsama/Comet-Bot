package io.github.starwishsama.nbot.objects

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.enums.MusicApi


class Config() {
    @SerializedName("botId")
    var botId = 0L
    @SerializedName("botPassword")
    lateinit var botPassword: String
    @SerializedName("auto_save_config_time")
    var autoSaveTime = 15
    @SerializedName("rcon_url")
    var rConUrl: String? = null
    @SerializedName("rcon_port")
    var rConPort = 0
    @SerializedName("rcon_password")
    var rConPassword: String? = null
    @SerializedName("netease_api")
    var netEaseApi: String? = null
    @SerializedName("cool_down_time")
    var coolDownTime = 7
    @SerializedName("filter_words")
    lateinit var filterWords: Array<String>
    @SerializedName("default_music_api")
    lateinit var musicApi: MusicApi
    @SerializedName("bilibili_username")
    var userName: String? = null
    @SerializedName("bilibili_password")
    var userPassword: String? = null
    @SerializedName("command_prefix")
    var commandPrefix: List<String> = mutableListOf(".", "。", "#", "!")
    @SerializedName("bili_user_name")
    var biliUserName: String? = null
    @SerializedName("bili_password")
    var biliPassword: String? = null
}