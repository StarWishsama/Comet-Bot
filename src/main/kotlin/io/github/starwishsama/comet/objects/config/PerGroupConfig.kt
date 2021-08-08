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

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.push.BiliBiliUser
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

    @JsonProperty("twitter_picture_mode")
    var twitterPictureMode: Boolean = true,

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
     * 是否关闭对此群消息的复读
     */
    @JsonProperty("repeat_function")
    var canRepeat: Boolean = true,

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
    val githubRepoSubscribers: MutableList<String> = mutableListOf(),

    @JsonProperty("old_file_clean_feature")
    var oldFileCleanFeature: Boolean = false,

    @JsonProperty("old_file_clean_delay")
    var oldFileCleanDelay: Long = 1000 * 60 * 60 * 24,

    @JsonProperty("old_file_match_pattern")
    var oldFileMatchPattern: String = "",

    @JsonProperty("disabled_commands")
    val disabledCommands: MutableSet<String> = mutableSetOf()
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

    data class ReplyKeyWord(
        val keyWords: MutableList<String> = mutableListOf(),
        val reply: MessageWrapper
    )
}