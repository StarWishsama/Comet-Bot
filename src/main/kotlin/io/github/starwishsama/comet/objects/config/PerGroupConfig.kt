package io.github.starwishsama.comet.objects.config

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.push.YoutubeUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class PerGroupConfig(
    @JsonProperty("group_id")
    val id: Long,
    @JsonProperty("version")
    val version: String = "2",

    /**
     * 是否自动接受入群请求
     */
    @JsonProperty("auto_accept")
    var autoAccept: Boolean = false,

    @JsonProperty("group_helpers")
    var helpers: MutableSet<Long> = hashSetOf(),

    /**
     * 推文推送服务
     */
    @JsonProperty("twitter_push_function")
    var twitterPushEnabled: Boolean = false,

    /**
     * Twitter 订阅列表
     */
    @JsonProperty("twitter_sub")
    var twitterSubscribers: MutableSet<String> = hashSetOf(),

    /**
     * bilibili 开播提醒服务
     */
    @JsonProperty("bili_push_function")
    var biliPushEnabled: Boolean = false,

    /**
     * bilibili 订阅列表
     */
    @JsonProperty("bili_sub")
    val biliSubscribers: MutableSet<BiliBiliUser> = hashSetOf(),

    /**
     * Youtube 开播推送服务
     */
    @JsonProperty("youtube_push_function")
    var youtubePushEnabled: Boolean = false,

    /**
     * Youtube 订阅列表
     */
    @JsonProperty("youtube_sub")
    val youtubeSubscribers: MutableSet<YoutubeUser> = hashSetOf(),

    /**
     * 是否关闭对此群消息的复读
     */
    @JsonProperty("repeat_function")
    var canRepeat: Boolean = true,

    /**
     * 本群启用的命令
     */
    @JsonProperty("disabled_commands")
    val disabledCommands: MutableSet<String> = mutableSetOf(),

    @JsonProperty("filter_words")
    val groupFilterWords: MutableList<String> = mutableListOf(),

    @JsonProperty("keyword_reply")
    val keyWordReply: MutableList<ReplyKeyWord> = mutableListOf(),

    @JsonProperty("newcomer_welcome")
    var newComerWelcome: Boolean = false,

    @JsonProperty("newcomer_welcome_text")
    var newComerWelcomeText: MessageWrapper = MessageWrapper().setUsable(true),

    @JsonProperty("auto_accept_condition")
    var autoAcceptCondition: String = "",

    @JsonProperty("github_repo_subs")
    val githubRepoSubscribers: MutableList<String> = mutableListOf()
) {

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