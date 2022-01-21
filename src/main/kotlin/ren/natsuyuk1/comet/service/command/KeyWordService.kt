/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.service.command

import io.github.starwishsama.comet.commands.chats.KeyWordCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.config.getAutoReplyByKeyWord
import io.github.starwishsama.comet.objects.wrapper.toMessageWrapper
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import net.mamoe.mirai.message.data.MessageChain

object KeyWordService {
    private val inProgressAdder = mutableMapOf<Long, String>()

    fun addKeyWord(senderID: Long, groupID: Long, keyWord: String): MessageChain {
        if (keyWord.isEmpty()) {
            return "请输入关键词".toChain()
        }

        SessionHandler.insertSession(Session(SessionTarget(groupID, senderID), KeyWordCommand))
        inProgressAdder[senderID] = keyWord

        return "接下来, 请发送该关键词需要自动回复的内容".toChain()
    }

    fun removeKeyWord(id: Long, keyWord: String): MessageChain {
        if (keyWord.isEmpty()) {
            return "请输入关键词".toChain()
        }

        val groupCfg = GroupConfigManager.getConfigOrNew(id)
        val autoReply = groupCfg.keyWordReply.getAutoReplyByKeyWord(keyWord)

        if (autoReply != null) {
            groupCfg.keyWordReply.remove(autoReply)
        }

        return "已移除关键词: $keyWord".toChain()
    }

    fun handleAddAutoReply(trigger: Long, cfg: PerGroupConfig, keyWord: String, reply: MessageChain): MessageChain {
        if (keyWord.isEmpty()) {
            return "请输入关键词".toChain()
        }

        if (reply.isEmpty()) {
            return "请输入自动回复内容".toChain()
        }

        val autoReply = cfg.keyWordReply.getAutoReplyByKeyWord(keyWord)

        if (autoReply != null) {
            autoReply.keyWord = keyWord
            autoReply.reply = reply.toMessageWrapper()
        } else {
            cfg.keyWordReply.add(
                PerGroupConfig.ReplyKeyWord(
                    keyWord,
                    reply.toMessageWrapper()
                )
            )
        }

        inProgressAdder.remove(trigger)

        return "已添加关键词: $keyWord".toChain()
    }

    fun listKeyWords(groupID: Long): MessageChain {
        val groupCfg = GroupConfigManager.getConfigOrNew(groupID)

        val result = buildString {
            append("关键词列表: \n")
            groupCfg.keyWordReply.forEach {
                append("${it.keyWord} -> ${it.reply.getAllText().limitStringSize(10)} \n")
            }
        }

        return result.toChain()
    }

    fun getKeyWordBySender(id: Long): String {
        return inProgressAdder[id].also { inProgressAdder.remove(id) } ?: ""
    }
}