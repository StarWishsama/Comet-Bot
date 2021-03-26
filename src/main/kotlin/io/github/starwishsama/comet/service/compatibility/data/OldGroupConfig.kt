package io.github.starwishsama.comet.service.compatibility.data

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.push.YoutubeUser

data class VersionTestObject(
    /**
     * 配置文件版本号
     */
    @JsonProperty("version")
    val version: String
)

data class OldGroupConfig(@JsonProperty("group_id") val id: Long) {
    /**
     * 是否自动接受入群请求
     */
    @JsonProperty("auto_accept")
    var autoAccept: Boolean = false

    @JsonProperty("group_helpers")
    var helpers: MutableSet<Long> = hashSetOf()

    /**
     * 推文推送服务
     */
    @JsonProperty("twitter_push_function")
    var twitterPushEnabled: Boolean = false

    /**
     * Twitter 订阅列表
     */
    @JsonProperty("twitter_sub")
    var twitterSubscribers: MutableSet<String> = hashSetOf()

    /**
     * bilibili 开播提醒服务
     */
    @JsonProperty("bili_push_function")
    var biliPushEnabled: Boolean = false

    /**
     * bilibili 订阅列表
     */
    @JsonProperty("bili_sub")
    val biliSubscribers: MutableSet<Long> = hashSetOf()

    /**
     * Youtube 开播推送服务
     */
    @JsonProperty("youtube_push_function")
    var youtubePushEnabled: Boolean = false

    /**
     * Youtube 订阅列表
     */
    @JsonProperty("youtube_sub")
    val youtubeSubscribers: MutableSet<YoutubeUser> = hashSetOf()

    /**
     * 是否关闭对此群消息的复读
     */
    @JsonProperty("repeat_function")
    var doRepeat: Boolean = true

    /**
     * 本群启用的命令
     */
    @JsonProperty("disabled_commands")
    val disabledCommands: MutableSet<String> = mutableSetOf()

    @JsonProperty("filter_words")
    val groupFilterWords: MutableList<String> = mutableListOf()

    @JsonProperty("keyword_reply")
    val keyWordReply: MutableList<PerGroupConfig.ReplyKeyWord> = mutableListOf()
}