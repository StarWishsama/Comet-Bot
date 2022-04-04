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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.MiraiInternalApi
import java.io.File

fun buildMessageWrapper(builder: MessageWrapper.() -> Unit): MessageWrapper {
    return MessageWrapper().apply(builder)
}

object EmptyMessageWrapper : MessageWrapper()

private val storedLocation = FileUtil.getChildFolder("messagewrapper")

@kotlinx.serialization.Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
open class MessageWrapper {
    private val messageContent = mutableSetOf<WrapperElement>()

    @JsonIgnore
    private lateinit var lastInsertElement: WrapperElement

    @JsonIgnore
    @Volatile
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

            lastInsertElement = if (lastInsertElement is PureText && element is PureText) {
                messageContent.remove(lastInsertElement)
                val merge = PureText((lastInsertElement as PureText).text + element.text)
                messageContent.add(merge)
                merge
            } else {
                messageContent.add(element)
                element
            }
        }

        return this
    }

    fun addText(text: String): MessageWrapper {
        addElement(PureText(text))
        return this
    }

    fun addPictureByURL(url: String?, imageFormat: String = ""): MessageWrapper {
        if (url == null) return this

        addElement(Picture(url, fileFormat = imageFormat))
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
                kotlin.runCatching {
                    if ((it is Picture && !isPictureReachLimit()) || it !is Picture) {
                        add(it.toMessageContent(subject))
                    }
                }.onFailure {
                    CometVariables.daemonLogger.log(HinaLogLevel.Warn, prefix = "MessageWrapper", throwable = it, message = "在转换消息时出现了问题")
                }
            }
        }.build()
    }

    fun removeElementsByClass(type: Class<*>): MessageWrapper =
        MessageWrapper().setUsable(usable).also {
            it.addElements(getMessageContent().filter { mw -> mw.className == type.name })
        }

    private fun isPictureReachLimit(): Boolean {
        return messageContent.count { it is Picture } > 9
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

@OptIn(MiraiInternalApi::class)
fun MessageChain.toMessageWrapper(localImage: Boolean = false): MessageWrapper {
    val wrapper = MessageWrapper()
    for (message in this) {
        when (message) {
            is PlainText -> {
                wrapper.addText(message.content)
            }
            is Image -> {
                runBlocking {
                    if (localImage) {
                        val location = NetUtil.downloadFile(storedLocation, message.queryUrl(), message.imageId)
                        wrapper.addElement(Picture(filePath = location.canonicalPath))
                    } else {
                        wrapper.addPictureByURL(message.queryUrl())
                    }
                }
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