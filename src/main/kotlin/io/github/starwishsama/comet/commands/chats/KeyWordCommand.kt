/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.CometVariables.localizationManager
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.KeyWordService
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class KeyWordCommand : ChatCommand, ConversationCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (!hasPermission(user, event)) {
            return localizationManager.getLocalizationText("message.no-permission").toChain()
        }

        if (event !is GroupMessageEvent) {
            return "请在群聊中使用该命令!".toChain()
        }

        if (args.isEmpty()) {
            return getHelp().toChain()
        }

        return when (args[0]) {
            "add", "new" -> KeyWordService.addKeyWord(event.sender.id, event.group.id, args.getOrElse(1) { "" }, this)
            "remove", "del", "rm" -> KeyWordService.removeKeyWord(event.group.id, args.getOrElse(1) { "" })
            "list", "ls" -> KeyWordService.listKeyWords(event.group.id)
            else -> getHelp().toChain()
        }
    }

    override val props: CommandProps
        get() = CommandProps(
            name = "keyword",
            aliases = listOf("关键词", "keywords", "kw", "gjc"),
            description = "查看当前`关键词",
            level = UserLevel.USER
        )

    override fun getHelp(): String =
        """
            /keyword add/new <关键词> - 添加关键词
            /keyword remove/del/rm <关键词> - 删除关键词
            /keyword list/ls - 查看关键词列表
        """.trimIndent()

    private fun hasPermission(user: CometUser, e: MessageEvent): Boolean {
        if (e is GroupMessageEvent) {
            return e.group.getMember(user.id)?.isAdministrator() ?: false
        }

        return true
    }

    override suspend fun handle(event: MessageEvent, user: CometUser, session: Session) {
        if (event is GroupMessageEvent) {
            val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
            val keyword = KeyWordService.getKeyWordBySender(event.sender.id)
            event.subject.sendMessage(KeyWordService.handleAddAutoReply(cfg, keyword, event.message))
        }
    }
}