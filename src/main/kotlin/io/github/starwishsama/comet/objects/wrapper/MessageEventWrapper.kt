package io.github.starwishsama.comet.objects.wrapper

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource

abstract class MessageEventWrapper {
    /**
     * 与这个消息事件相关的 [Bot]
     */
    abstract val bot: Bot

    /**
     * 消息事件主体.
     *
     * - 对于好友消息, 这个属性为 [Friend] 的实例, 与 [sender] 引用相同;
     * - 对于临时会话消息, 这个属性为 [Member] 的实例, 与 [sender] 引用相同;
     * - 对于群消息, 这个属性为 [Group] 的实例, 与 [GroupMessageEvent.group] 引用相同
     *
     * 在回复消息时, 可通过 [subject] 作为回复对象
     */
    abstract val subject: Contact

    /**
     * 发送人.
     *
     * 在好友消息时为 [Friend] 的实例, 在群消息时为 [Member] 的实例
     */
    abstract val sender: User

    /**
     * 发送人名称
     */
    abstract val senderName: String

    /**
     * 消息内容.
     *
     * 第一个元素一定为 [MessageSource], 存储此消息的发送人, 发送时间, 收信人, 消息 id 等数据.
     * 随后的元素为拥有顺序的真实消息内容.
     */
    abstract val message: MessageChain

    /** 消息发送时间 (由服务器提供, 可能与本地有时差) */
    abstract val time: Int

    /**
     * 消息源. 来自 [message] 的第一个元素,
     */
    abstract val source: OnlineMessageSource.Incoming

}