/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import java.util.*

/**
 * [PushContext]
 *
 * 推送内容
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(value = BiliBiliDynamicContext::class, name = "bili_dynamic"),
    JsonSubTypes.Type(value = BiliBiliLiveContext::class, name = "bili_live"),
    JsonSubTypes.Type(value = TwitterContext::class, name = "tweet")
)
abstract class PushContext(
    /**
     * 需要被推送的对象
     */
    private val pushTarget: MutableSet<Long>,
    /**
     * 获取该内容的时间, 为 [currentTimeMillis]
     */
    var retrieveTime: Long,
    /**
     * 该内容的状态, 详见 [PushStatus]
     */
    open var status: PushStatus
) : Pushable {
    /**
     * 添加一个推送对象
     */
    fun addPushTarget(id: Long) {
        pushTarget.add(id)
    }

    /**
     * 清空所有推送对象
     */
    fun clearPushTarget() {
        pushTarget.clear()
    }

    /**
     * 获取推送对象的不可变副本 [UnmodifiableSet]
     */
    fun getPushTarget(): Set<Long> = Collections.unmodifiableSet(pushTarget)

    override fun toMessageWrapper(): MessageWrapper {
        throw UnsupportedOperationException("Base PushContext can't convert to MessageWrapper")
    }

    override fun contentEquals(other: PushContext): Boolean {
        return this == other
    }
}

enum class PushStatus {
    CREATED, PROGRESSING, PUSHING, PUSHED
}