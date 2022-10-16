package ren.natsuyuk1.comet.api.session

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

fun Session.register() = SessionManager.registerSession(this)

fun Session.registerTimeout(timeout: Duration) = SessionManager.registerTimeoutSession(this, timeout)

fun Session.expire() = SessionManager.expireSession(this)

object SessionManager {
    private var scope = ModuleScope("comet-session-manager")

    private val sessions = ConcurrentLinkedQueue<Session>()

    private val autoExpireJobs = ConcurrentHashMap<Session, Job>()

    fun init(parentContext: CoroutineContext) {
        scope = ModuleScope(scope.name(), parentContext)
    }

    /**
     * 注册一个会话 [Session] 以备监听
     */
    fun registerSession(session: Session) {
        logger.debug { "Registering session ${session::class.simpleName}#${session.hashCode()}" }

        sessions.add(session)
    }

    /**
     * 注册一个 [Session] 以备监听
     * 并且这个会话会在指定时间后自动注销
     *
     * @param session 欲注册的会话
     * @param timeout 自动注销的期限
     */
    fun registerTimeoutSession(session: Session, timeout: Duration) {
        registerSession(session)

        val job = TaskManager.registerTaskDelayed(timeout) {
            expireSession(session)
        }

        autoExpireJobs[session] = job
    }

    /**
     * 注销一个会话 [Session]
     */
    fun expireSession(session: Session) {
        logger.debug { "Expiring session ${session::class.simpleName}#${session.hashCode()}" }

        sessions.remove(session)

        autoExpireJobs[session]?.cancel()
        autoExpireJobs.remove(session)
    }

    /**
     * 处理会话
     *
     * @param subject 可能触发的联系人 [Contact]
     * @param message 触发时发送的消息
     */
    fun handleSession(subject: Contact, sender: Contact, message: MessageWrapper): Boolean {
        val user: CometUser? = CometUser.getUser(sender.id, sender.platform)

        logger.debug { "Processing incoming subject: ${subject.id}, sender: ${sender.id}, message: $message" }
        logger.debug { "Pending session(s): $sessions" }

        val targetSession = sessions.filter { session ->
            if (subject is Group) {
                return@filter session.cometUser == null || (user != null && session.cometUser.id == user.id)
            } else {
                if (session.cometUser != null && user != null) {
                    return@filter session.cometUser.id == user.id
                } else {
                    return@filter sender.id == session.contact.id
                }
            }
        }

        logger.debug { "Matched session(s): $targetSession" }

        targetSession.forEach { scope.launch { it.process(message) } }

        return targetSession.isNotEmpty()
    }

    fun getSessionCount() = sessions.size
}
