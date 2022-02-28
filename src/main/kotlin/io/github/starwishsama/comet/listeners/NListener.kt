/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.CometVariables
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf

/**
 * Comet 监听器
 *
 * 可以实现 [INListener] 后添加不同的方法
 * 并加上 [EventHandler] 注解处理不同的 [Event]
 */
interface INListener {
    val name: String
}

/**
 * 快速注册该监听器到一个 [Bot] 上
 *
 * @param bot Bot 实例
 */
fun INListener.register(bot: Bot) {
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
        CometVariables.daemonLogger.warning("监听器 ${clazz.java.simpleName} 没有监听任何一个事件!")
        return
    } else {
        methodEvent.forEach { (clazz, method) ->
            if (clazz.isSubclassOf(Event::class)) {
                @Suppress("UNCHECKED_CAST")
                bot.globalEventChannel().subscribeAlways(clazz) { subEvent ->
                    if (CometVariables.switch) {
                        // Don't handle self message to avoid ban
                        if (subEvent is MessageEvent && subEvent.sender.id == subEvent.bot.id) {
                            return@subscribeAlways
                        }

                        try {
                            method.call(this@register, subEvent)
                        } catch (e: Exception) {
                            CometVariables.daemonLogger.warning("${this@register.name} 在运行时发生了异常", e.cause)
                        }
                    }
                }
            }
        }
    }

    CometVariables.logger.info("[监听器] 已注册 $name 监听器")

}