package io.github.starwishsama.comet.sessions

import net.mamoe.mirai.contact.Member


/**
 * @author Nameless
 */
open class SessionUser(open val userId: Long, val userName: String = "未知", val member: Member? = null)