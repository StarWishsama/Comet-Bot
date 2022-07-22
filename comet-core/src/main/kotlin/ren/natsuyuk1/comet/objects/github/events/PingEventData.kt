/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects.github.events

import kotlinx.serialization.SerialName
import ren.natsuyuk1.comet.objects.github.api.RepoInfo
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper

/**
 * [PingEventData]
 *
 * 该事件通常只在首次添加时触发.
 */
data class PingEventData(
    val zen: String,
    @SerialName("hook_id")
    val hookID: Long,
    @SerialName("hook")
    val hookInfo: HookInfo,
    @SerialName("repository")
    val repositoryInfo: RepoInfo
) : GithubEventData {
    data class HookInfo(
        val type: String,
        val id: Long,
        val name: String,
        val active: Boolean,
        val events: List<String>
    )

    // Ping 事件无需转换
    override fun toMessageWrapper(): MessageWrapper {
        return buildMessageWrapper {
            appendText("🎉 成功添加 ${repositoryInfo.repoFullName} 仓库的推送")
        }
    }

    override fun repoName(): String {
        return repositoryInfo.repoFullName
    }

    override fun branchName(): String {
        return ""
    }

    override fun type(): String = "ping"

    override fun isSendableEvent(): Boolean = true
}