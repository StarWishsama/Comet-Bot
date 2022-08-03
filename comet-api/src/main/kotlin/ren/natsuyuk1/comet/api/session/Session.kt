package ren.natsuyuk1.comet.api.session

import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.message.MessageWrapper

abstract class Session(
    /**
     * 此会话的目标对象, 可以是个人或群聊
     */
    val contact: PlatformCommandSender,
    /**
     * 此会话的目标对象的 [CometUser] 实例, 仅在对象为个人时不为空
     */
    val cometUser: CometUser?
) {
    abstract fun handle(message: MessageWrapper)
}