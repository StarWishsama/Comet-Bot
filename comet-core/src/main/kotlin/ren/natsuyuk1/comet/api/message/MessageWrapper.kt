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

/**
 * 代表一个空的 [MessageWrapper]
 */
object EmptyMessageWrapper : MessageWrapper()

@kotlinx.serialization.Serializable
open class MessageWrapper {
    private val messageContent = mutableSetOf<WrapperElement>()

    @kotlinx.serialization.Transient
    private lateinit var lastInsertElement: WrapperElement

    @kotlinx.serialization.Transient
    private var usable: Boolean = isEmpty()

    fun appendText(text: String, autoNewline: Boolean = false): MessageWrapper =
        apply {
            appendElement(Text(text))
            if (autoNewline) appendLine()
        }

    fun appendLine(): MessageWrapper = apply { appendElement(Text("\n")) }

    fun appendElement(element: WrapperElement): MessageWrapper =
        apply { appendElements(element) }

    fun appendElements(vararg element: WrapperElement): MessageWrapper =
        apply { appendElements(element.toList()) }

    fun appendElements(elements: Collection<WrapperElement>): MessageWrapper =
        apply {
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
        }

    fun setUsable(usable: Boolean): MessageWrapper = apply { this.usable = usable }

    fun isUsable(): Boolean = usable

    fun removeElementsByClass(type: Class<*>): MessageWrapper =
        MessageWrapper().setUsable(usable).also {
            it.appendElements(getMessageContent().filter { mw -> mw.className == type.name })
        }

    private fun isPictureReachLimit(): Boolean = messageContent.count { it is Image } > 9

    override fun toString(): String = "MessageWrapper {content=${messageContent}, usable=${usable}}"

    fun parseToString(): String = buildString {
        messageContent.forEach {
            append(it.asString())
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
