package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import kotlin.reflect.KClass

object GroupRequestListener: NListener {
    override val eventToListen: List<KClass<out Event>> = listOf(MemberJoinRequestEvent::class)

    override fun listen(event: Event) {
        if (event is MemberJoinRequestEvent) {
            val cfg = GroupConfigManager.getConfig(event.groupId) ?: return

            if (cfg.autoAccept) {
                runBlocking {
                    if (cfg.autoAcceptCondition.isEmpty()) {
                        event.accept()
                    } else {
                        if (cfg.autoAcceptCondition == event.message) {
                            event.accept()
                        } else {
                            /**
                             * @TODO 添加自定义拒绝理由
                             */
                            event.reject(false, "")
                        }
                    }
                }
            }
        }
    }

    override fun getName(): String = "入群申请自动通过"
}