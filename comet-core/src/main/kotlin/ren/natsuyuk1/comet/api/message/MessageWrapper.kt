/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.message

import java.util.*

inline fun buildMessageWrapper(builder: MessageWrapper.() -> Unit): MessageWrapper {
    return MessageWrapper().apply(builder)
}

object EmptyMessageWrapper : MessageWrapper()

@kotlinx.serialization.Serializable
open class MessageWrapper {
    private val messageContent = mutableSetOf<WrapperElement>()

    @kotlinx.serialization.Transient
    private lateinit var lastInsertElement: WrapperElement

    @kotlinx.serialization.Transient
    private var usable: Boolean = isEmpty()

    fun addElement(element: WrapperElement): MessageWrapper {
        addElements(element)
        return this
    }

    fun addElements(vararg element: WrapperElement): MessageWrapper {
        addElements(element.toList())
        return this
    }

    fun addElements(elements: Collection<WrapperElement>): MessageWrapper {
        for (element in elements) {
            if (!::lastInsertElement.isInitialized) {
                lastInsertElement = element
                messageContent.add(element)
                continue
            }

            lastInsertElement = if (lastInsertElement is Text && element is Text) {
                messageContent.remove(lastInsertElement)
                val merge = Text((lastInsertElement as Text).text + element.text)
                messageContent.add(merge)
                merge
            } else {
                messageContent.add(element)
                element
            }
        }

        return this
    }

    fun setUsable(usable: Boolean): MessageWrapper {
        this.usable = usable
        return this
    }

    fun isUsable(): Boolean {
        return usable
    }

    fun removeElementsByClass(type: Class<*>): MessageWrapper =
        MessageWrapper().setUsable(usable).also {
            it.addElements(getMessageContent().filter { mw -> mw.className == type.name })
        }

    private fun isPictureReachLimit(): Boolean {
        return messageContent.count { it is Image } > 9
    }

    override fun toString(): String {
        return "MessageWrapper {content=${messageContent}, usable=${usable}}"
    }

    fun parseToString(): String {
        return buildString {
            messageContent.forEach {
                append(it.asString())
            }
        }
    }

    fun getMessageContent(): Set<WrapperElement> = Collections.unmodifiableSet(messageContent)

    fun compare(other: Any?): Boolean {
        if (other !is MessageWrapper) return false

        return getMessageContent() == other.getMessageContent()
    }

    fun isEmpty(): Boolean {
        return messageContent.isEmpty()
    }
}
