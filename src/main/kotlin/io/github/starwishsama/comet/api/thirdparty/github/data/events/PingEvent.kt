/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.github.data.events

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.github.data.api.RepoInfo
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class PingEvent(
    val zen: String,
    @JsonProperty("hook_id")
    val hookID: Long,
    @JsonProperty("hook")
    val hookInfo: HookInfo,
    @JsonProperty("repository")
    val repositoryInfo: RepoInfo
) : GithubEvent {
    data class HookInfo(
        val type: String,
        val id: Long,
        val name: String,
        val active: Boolean,
        val events: List<String>
    )

    // Ping 事件无需转换
    override fun toMessageWrapper(): MessageWrapper {
        return MessageWrapper().setUsable(false)
    }

    override fun repoName(): String {
        return repositoryInfo.repoFullName
    }

    override fun isSendableEvent(): Boolean = false
}