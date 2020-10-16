package io.github.starwishsama.comet.objects.group

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand

data class PerGroupConfig(@SerializedName("group_id") val id: Long) {

    /**
     * 是否自动接受入群请求
     */
    @SerializedName("auto_accept")
    var autoAccept: Boolean = false

    @SerializedName("group_helpers")
    var helpers: MutableSet<Long> = hashSetOf()

    /**
     * 推文推送服务
     */
    @SerializedName("twitter_push_function")
    var twitterPushEnabled: Boolean = false

    /**
     * Twitter 订阅列表
     */
    @SerializedName("twitter_sub")
    var twitterSubscribers: MutableSet<String> = hashSetOf()

    /**
     * bilibili 开播提醒服务
     */
    @SerializedName("bili_push_function")
    var biliPushEnabled: Boolean = false

    /**
     * bilibili 订阅列表
     */
    @SerializedName("bili_sub")
    val biliSubscribers: MutableSet<Long> = hashSetOf()

    /**
     * Youtube 开播推送服务
     */
    @SerializedName("youtube_push_function")
    var youtubePushEnabled: Boolean = false

    /**
     * Youtube 订阅列表
     */
    @SerializedName("youtube_sub")
    val youtubeSubscribers: MutableSet<String> = hashSetOf()

    /**
     * 是否关闭对此群消息的复读
     */
    @SerializedName("repeat_function")
    var doRepeat: Boolean = true

    /**
     * 本群启用的命令
     */
    @SerializedName("disabled_commands")
    val disabledCommands: MutableSet<String> = mutableSetOf()

    fun addHelper(id: Long): Boolean {
        if (isHelper(id)) return false
        helpers.add(id)
        return true
    }

    fun removeHelper(id: Long): Boolean {
        if (!isHelper(id)) return false
        helpers.remove(id)
        return true
    }

    fun isHelper(id: Long): Boolean {
        return helpers.contains(id)
    }

    fun init(): PerGroupConfig {
        BotVariables.perGroup.add(this)
        return this
    }

    fun isDisabledCommand(command: ChatCommand): Boolean {
        return try {
            disabledCommands.contains(command.name)
        } catch (npe: NullPointerException) {
            false
        }
    }
}