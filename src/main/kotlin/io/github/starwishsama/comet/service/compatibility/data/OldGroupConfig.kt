/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.compatibility.data

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.objects.config.PerGroupConfig

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
    val biliSubscribers: MutableSet<Int> = hashSetOf()

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