package ren.natsuyuk1.comet.api.listener

import mu.KotlinLogging
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.CometEvent
import ren.natsuyuk1.comet.api.event.Event
import ren.natsuyuk1.comet.api.event.events.message.MessageEvent
import ren.natsuyuk1.comet.api.event.registerListener
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

private val logger = KotlinLogging.logger {}

/**
 * Comet 的监听器
 *
 * 监听时需使用 [EventHandler] 注解
 */
interface CometListener {
    val name: String
}

@Target(AnnotationTarget.FUNCTION)
annotation class EventHandler

fun CometListener.register(comet: Comet) {
    val clazz = this::class

    val methodEvent = mutableMapOf<KClass<out Event>, KFunction<*>>()

    clazz.functions.forEach {
        if (it.annotations.find { clazz -> clazz.annotationClass == EventHandler::class } == null) {
            return@forEach
        } else {
            it.parameters.forEach { kp ->
                val eventClass = kp.type.classifier

                if (kp.type.isSubtypeOf(Event::class.createType()) && eventClass != null && eventClass is KClass<*>) {
                    @Suppress("UNCHECKED_CAST")
                    methodEvent[eventClass as KClass<out Event>] = it
                }
            }
        }
    }

    if (name.isEmpty() || methodEvent.isEmpty()) {
        logger.warn("监听器 ${clazz.java.simpleName} 没有监听任何一个事件!")
        return
    } else {
        methodEvent.forEach { (clazz, method) ->
            if (clazz.isSubclassOf(CometEvent::class)) {
                registerListener(clazz) { subEvent ->
                    if (subEvent is CometEvent) {
                        if (subEvent.comet != comet) {
                            return@registerListener
                        }

                        // Don't handle message event from bot itself
                        if (subEvent is MessageEvent && subEvent.sender.id == comet.id) {
                            return@registerListener
                        }

                        try {
                            if (method.isSuspend) {
                                method.callSuspend(this@register, subEvent)
                            } else {
                                method.call(this@register, subEvent)
                            }
                        } catch (e: InvocationTargetException) {
                            logger.warn(e.cause) { "${this@register.name} 在运行时发生了异常" }
                        } catch (e: Throwable) {
                            logger.warn(e) { "${this@register.name} 在运行时发生了异常" }
                        }
                    }
                }
            }
        }
    }

    logger.info("[监听器] 已注册 $name 监听器")
}
