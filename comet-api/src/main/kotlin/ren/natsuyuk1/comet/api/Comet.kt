/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KLogger
import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.event.impl.message.MessageEvent
import ren.natsuyuk1.comet.api.event.registerListener
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import kotlin.coroutines.CoroutineContext

/**
 * [Comet] 代表单个对应多平台的机器人实例
 *
 * 不同平台应实现此实例
 *
 * 使用前请先在前端侧 init
 *
 */
abstract class Comet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    val config: CometConfig,

    /**
     * 一个 Comet 实例的 [KLogger]
     */
    val logger: KLogger,

    /**
     * 一个 Comet 实例的 [ModuleScope]
     */
    var scope: ModuleScope
) : IComet {
    lateinit var initTime: Instant

    abstract val id: String

    fun init(parentContext: CoroutineContext) {
        scope = ModuleScope(scope.name(), parentContext)

        initTime = Clock.System.now()
    }

    abstract fun login()

    abstract fun afterLogin()

    abstract fun close()
}

fun Comet.attachMessageProcessor() {
    registerListener<MessageEvent> {
        if (it.comet == this) {
            val sessionProcessed = SessionManager.handleSession(it.subject, it.message)

            if (!sessionProcessed)
                CommandManager.executeCommand(
                    comet = this,
                    sender = it.sender,
                    subject = it.subject,
                    wrapper = it.message
                )
        }
    }
}
