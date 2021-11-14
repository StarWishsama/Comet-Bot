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

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.ArkNightService.configGachaPool
import io.github.starwishsama.comet.service.command.ArkNightService.getGachaResult
import io.github.starwishsama.comet.service.command.ArkNightService.handleFreedomDraw
import io.github.starwishsama.comet.service.gacha.GachaService
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

@Suppress("SpellCheckingInspection")
class ArkNightCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (!GachaService.isArkNightUsable()) {
            return if (GachaService.isDownloading()) {
                "正在下载明日方舟数据, 请稍候...".toChain()
            } else {
                GachaService.downloadArkNightData()
                "还未下载明日方舟数据, 开始自动下载...".toChain()
            }
        }

        if (args.isNotEmpty()) {
            return when (args[0]) {
                "单次寻访", "1", "单抽" -> {
                    getGachaResult(event, user, 1)
                }
                "十连寻访", "10", "十连" -> {
                    getGachaResult(event, user, 10)
                }
                "一井", "300", "来一井" -> {
                    getGachaResult(event, user, 300)
                }
                "pool", "卡池", "卡池信息" -> {
                    configGachaPool(args)
                }
                "update", "更新", "更新数据" -> {
                    GachaService.downloadArkNightData(true)
                    "已开始自动下载明日方舟数据...".toChain()
                }
                else -> handleFreedomDraw(event, user, args)
            }
        } else {
            return getHelp().convertToChain()
        }
    }

    override val props: CommandProps =
        CommandProps(
            "arknight",
            arrayListOf("ark", "xf", "方舟寻访"),
            "明日方舟寻访模拟器",

            UserLevel.USER,
            consumePoint = 5.0
        )

    override fun getHelp(): String = """
         /ark 单次寻访/十连寻访/一井/[次数]
         /ark pool 修改抽卡卡池
    """.trimIndent()
}