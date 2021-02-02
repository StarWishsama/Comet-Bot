package io.github.starwishsama.comet.objects.config

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.push.YoutubeUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

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
    val biliSubscribers: MutableSet<BiliBiliUser> = hashSetOf()

    /**
     * Youtube 开播推送服务
     */
    @SerializedName("youtube_push_function")
    var youtubePushEnabled: Boolean = false

    /**
     * Youtube 订阅列表
     */
    @SerializedName("youtube_sub")
    val youtubeSubscribers: MutableSet<YoutubeUser> = hashSetOf()

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

    @SerializedName("filter_words")
    val groupFilterWords: MutableList<String> = mutableListOf()

    @SerializedName("keyword_reply")
    val keyWordReply: MutableList<ReplyKeyWord> = mutableListOf()

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

    fun init() {
        GroupConfigManager.getAllConfigs().forEach {
            if (it.id == id) {
                return
            }
        }

        GroupConfigManager.addConfig(this)
    }

    fun isDisabledCommand(command: ChatCommand): Boolean {
        return try {
            disabledCommands.contains(command.name)
        } catch (npe: NullPointerException) {
            false
        }
    }

    fun disableCommand(commandName: String): ConfigureCommandStatus {
        val command = CommandExecutor.getCommand(commandName)
        if (command != null) {
            if (command is UnDisableableCommand) {
                return ConfigureCommandStatus.UnDisabled
            }

            return if (!disabledCommands.contains(command.name)) {
                disabledCommands.add(command.name)
                ConfigureCommandStatus.Disabled
            } else {
                disabledCommands.remove(command.name)
                ConfigureCommandStatus.Enabled
            }
        } else {
            return ConfigureCommandStatus.NotExist
        }
    }

    sealed class ConfigureCommandStatus(val msg: String) {
        object UnDisabled: ConfigureCommandStatus("该命令无法被禁用!")
        object Enabled: ConfigureCommandStatus("成功启用该命令")
        object Disabled: ConfigureCommandStatus("成功禁用该命令")
        object NotExist: ConfigureCommandStatus("该命令不存在!")
    }

    data class ReplyKeyWord(
        val keyWords: MutableList<String> = mutableListOf(),
        val reply: MessageWrapper
    )
}