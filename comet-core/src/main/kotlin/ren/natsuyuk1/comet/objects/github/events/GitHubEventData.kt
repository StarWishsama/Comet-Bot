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

import ren.natsuyuk1.comet.api.message.MessageWrapper

/**
 * 代表一个通用的 GitHub 推送事件实例.
 */
interface GitHubEventData {
    /**
     * 转换为文本形式的推送内容
     *
     * @return 推送内容 [MessageWrapper]
     */
    fun toMessageWrapper(): MessageWrapper

    /**
     * 获取该推送事件的来源仓库名
     *
     * @return 仓库名
     */
    fun repoName(): String

    /**
     * 获取该推送事件的分支名
     *
     * @return 分支名
     */
    fun branchName(): String

    /**
     * 获取该推送事件的类型
     *
     * @return 类型
     */
    fun type(): String

    /**
     * 获取访问该推送事件的链接
     *
     * @return 链接
     */
    fun url(): String

    /**
     * 判断该事件是否为一个可推送的事件
     *
     * @return 该事件是否可推送
     */
    fun isSendableEvent(): Boolean
}
