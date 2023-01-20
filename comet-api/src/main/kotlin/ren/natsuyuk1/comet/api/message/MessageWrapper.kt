/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.message

import kotlinx.serialization.Transient
import java.util.*

inline fun buildMessageWrapper(receipt: MessageReceipt? = null, builder: MessageWrapper.() -> Unit): MessageWrapper {
    return MessageWrapper(receipt).apply(builder)
}

/**
 * 代表一个空的 [MessageWrapper]
 */
object EmptyMessageWrapper : MessageWrapper()

@kotlinx.serialization.Serializable
open class MessageWrapper(
    @Transient
    val receipt: MessageReceipt? = null
) {
    private val messageContent = mutableSetOf<WrapperElement>()

    @Transient
    private lateinit var lastInsertElement: WrapperElement

    @Transient
    private var usable: Boolean = isEmpty()

    fun appendText(text: String): MessageWrapper =
        apply {
            appendElement(Text(text))
        }

    fun appendTextln(text: String): MessageWrapper = appendText(text).appendLine()

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
            it.appendElements(getMessageContent().filter { mw -> mw::class.simpleName == type.name })
        }

    override fun toString(): String = "MessageWrapper {content=$messageContent, usable=$usable}"

    fun encodeToString(): String = buildString {
        messageContent.forEach {
            append(it.parseToString())
        }
    }

    fun getMessageContent(): Set<WrapperElement> = Collections.unmodifiableSet(messageContent)

    fun compare(other: MessageWrapper) = getMessageContent() == other.getMessageContent()

    fun isEmpty(): Boolean {
        return messageContent.isEmpty()
    }

    fun trim(): MessageWrapper {
        val last = messageContent.last()
        if (last is Text && last.text.endsWith("\n")) {
            messageContent.remove(last)
            messageContent.add(Text(last.text.removeSuffix("\n")))
        }

        return this
    }

    inline fun <reified T : WrapperElement> find(): T? =
        getMessageContent().asSequence().filterIsInstance<T>().firstOrNull()

    inline fun <reified T : WrapperElement> filterIsInstance(): List<T> =
        getMessageContent().asSequence().filterIsInstance<T>().toList()
}
