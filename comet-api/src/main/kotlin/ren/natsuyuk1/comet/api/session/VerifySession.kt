package ren.natsuyuk1.comet.api.session

import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import kotlin.time.Duration

/**
 * 用于防止平台风控及平台限制的会话
 */
abstract class VerifySession(
    /**
     * 此会话的目标对象, 可以是个人或群聊
     */
    contact: PlatformCommandSender,
    /**
     * 此会话的目标对象的 [CometUser] 实例, 仅在对象为个人时不为空
     */
    cometUser: CometUser?,
    /**
     * 该会话是否要打断命令解析.
     * 通常不阻塞命令解析的会话可用于引导用户主动发起回话, 避免风控.
     */
    interrupt: Boolean = true,
    /**
     * 验证后欲传递到要处理的会话 [Session]
     */
    private val passTo: Session,
) : Session(contact, cometUser, interrupt) {
    abstract fun verify(sender: PlatformCommandSender, message: MessageWrapper)

    // 不会触发 process
    final override suspend fun process(message: MessageWrapper) {}

    fun passSession() = passTo.register()

    fun passSession(timeout: Duration) = passTo.registerTimeout(timeout)
}
