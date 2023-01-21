package ren.natsuyuk1.comet.api

import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import java.util.concurrent.ConcurrentLinkedDeque

val cometInstances = ConcurrentLinkedDeque<Comet>()
internal val cometScope = ModuleScope("comet_global_scope")

/**
 * [IComet]
 *
 * 一个 [Comet] 扩展方法，提供一些 Bot 端的功能。
 */
interface IComet {
    /**
     * 使用一个已有 [MessageReceipt] 回复对应消息
     *
     * @param message 消息
     * @param receipt 消息回执
     */
    suspend fun reply(message: MessageWrapper, receipt: MessageReceipt): MessageReceipt?

    /**
     * 获取一个群聊
     *
     * @param id 群聊 ID
     *
     * @return [Group]，可能为空
     */
    suspend fun getGroup(id: Long): Group?

    /**
     * 撤回 / 删除一个已发送的信息
     *
     * @param source [MessageSource] 消息来源
     *
     * @return 是否成功撤回 / 删除此消息
     */
    suspend fun deleteMessage(source: MessageSource): Boolean

    /**
     * 获取机器人的一个好友 (QQ 独占)
     */
    suspend fun getFriend(id: Long): User?

    /**
     * 获取机器人的一个临时会话 (QQ 独占)
     */
    suspend fun getStranger(id: Long): User?
}
