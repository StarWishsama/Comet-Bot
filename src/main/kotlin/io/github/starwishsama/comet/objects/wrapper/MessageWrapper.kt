/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.wrapper

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import java.util.stream.Collectors

fun buildMessageWrapper(builder: MessageWrapper.() -> Unit): MessageWrapper {
    return MessageWrapper().apply(builder)
}

@JsonIgnoreProperties(ignoreUnknown = true)
open class MessageWrapper {
    private val messageContent = mutableSetOf<WrapperElement>()

    @Volatile
    private var usable: Boolean = isEmpty()

    fun addElement(element: WrapperElement): MessageWrapper {
        messageContent.add(element)
        return this
    }

    fun addElements(vararg element: WrapperElement): MessageWrapper {
        messageContent.addAll(element)
        return this
    }

    fun addElements(elements: Collection<WrapperElement>): MessageWrapper {
        messageContent.addAll(elements)
        return this
    }

    fun addText(text: String): MessageWrapper {
        messageContent.add(PureText(text))
        return this
    }

    fun addPictureByURL(url: String?, imageFormat: String = ""): MessageWrapper {
        if (url == null) return this

        messageContent.add(Picture(url, fileFormat = imageFormat))
        return this
    }

    fun setUsable(usable: Boolean): MessageWrapper {
        this.usable = usable
        return this
    }

    fun isUsable(): Boolean {
        return usable
    }

    /**
     * [toMessageChain]
     *
     * 将一个 [MessageWrapper] 转换为 [MessageChain]
     *
     * @param subject Mirai 的 [Contact], 为空时一些需要 [Contact] 的元素会转为文字
     */
    fun toMessageChain(subject: Contact? = null): MessageChain {
        return MessageChainBuilder().apply {
            messageContent.forEach {
                if (it is Picture) {
                    if (isPictureReachLimit()) {
                        return@forEach
                    }

                    if (subject == null) {
                        add("[图片]")
                    } else {
                        add(it.toMessageContent(subject))
                    }

                    return@forEach
                }

                add(it.toMessageContent(subject))
            }
        }.build()
    }

    fun removeElementsByClass(type: Class<*>): MessageWrapper =
        MessageWrapper().setUsable(usable).also {
            it.addElements((getMessageContent()).parallelStream().filter { mw -> mw.className == type.name }
                .collect(Collectors.toSet()))
        }

    private fun isPictureReachLimit(): Boolean {
        return messageContent.parallelStream().filter { it is Picture }.count() > 9
    }

    override fun toString(): String {
        return "MessageWrapper {content=${messageContent}}"
    }

    fun getAllText(): String {
        val texts = messageContent.parallelStream().filter { it is PureText }.collect(Collectors.toList())
        return buildString {
            texts.forEach {
                append(it.asString())
            }
        }
    }

    fun getMessageContent(): Set<WrapperElement> {
        return mutableSetOf<WrapperElement>().apply {
            addAll(messageContent)
        }
    }

    fun compare(other: Any?): Boolean {
        if (other !is MessageWrapper) return false

        return getMessageContent() == other.getMessageContent()
    }

    fun isEmpty(): Boolean {
        return messageContent.isEmpty()
    }
}

fun MessageChain.toMessageWrapper(): MessageWrapper {
    val wrapper = MessageWrapper()
    for (message in this) {
        when (message) {
            is PlainText -> {
                wrapper.addText(message.content)
            }
            is Image -> {
                runBlocking { wrapper.addPictureByURL(message.queryUrl()) }
            }
            is At -> {
                wrapper.addElement(AtElement(message.target))
            }
            is ServiceMessage -> {
                wrapper.addElement(XmlElement(message.content))
            }
            else -> {
                continue
            }
        }
    }

    return wrapper
}