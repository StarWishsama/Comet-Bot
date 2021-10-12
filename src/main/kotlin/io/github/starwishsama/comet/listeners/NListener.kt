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

import net.mamoe.mirai.event.Event
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubtypeOf

interface INListener

fun INListener.parseListener(): Pair<String, Map<KClass<out Event>, KFunction<*>>> {
    val clazz = this::class
    val nListener = clazz.annotations.firstOrNull { it.annotationClass == NListener::class }
        ?: return Pair("", emptyMap())

    val name = (nListener as NListener).name

    val methodEvent = mutableMapOf<KClass<out Event>, KFunction<*>>()

    clazz.functions.forEach {
        if (it.annotations.find { clazz -> clazz.annotationClass == EventHandler::class } == null) {
            return@forEach
        } else {
            it.parameters.forEach { kp ->
                if (kp.type.isSubtypeOf(Event::class.createType()) && kp.type.classifier != null && kp.type.classifier is KClass<*>) {
                    methodEvent[kp.type.classifier as KClass<out Event>] = it
                }
            }
        }
    }

    return Pair(name, methodEvent)
}