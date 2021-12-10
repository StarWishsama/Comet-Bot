/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.command

import io.github.starwishsama.comet.commands.chats.ArkNightCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.gacha.GachaResult
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.service.gacha.GachaService
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.GachaUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.uploadAsImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.at

object ArkNightService {
    private var pool = GachaService.getPoolsByType<ArkNightPool>()[0]

    suspend fun getGachaResult(event: MessageEvent, user: CometUser, time: Int): MessageChain {
        val gachaResult: GachaResult = pool.getArkDrawResult(user, time)
        return if (GachaUtil.arkPictureIsUsable()) {
            generatePictureGachaResult(pool, event, user, gachaResult)
        } else {
            pool.getArkDrawResultAsString(user, gachaResult).toChain()
        }
    }

    private suspend fun generatePictureGachaResult(
        pool: ArkNightPool,
        event: MessageEvent,
        user: CometUser,
        gachaResult: GachaResult
    ): MessageChain {
        event.subject.sendMessage("请稍等...")

        val ops = gachaResult.items

        return if (ops.isNotEmpty()) {
            // 只获取最后十个
            val result =
                GachaUtil.combineGachaImage(if (ops.size <= 10) ops else ops.subList(ops.size - 11, ops.size - 1), pool)
            if (result.lostItem.isNotEmpty())
                event.subject.sendMessage(
                    event.message.quote() + toChain(
                        "由于缺失资源文件, 以下干员无法显示 :(\n" +
                                buildString {
                                    result.lostItem.forEach {
                                        append("${it.name},")
                                    }
                                }.removeSuffix(",")
                    )
                )
            val gachaImage = withContext(Dispatchers.IO) { result.image.uploadAsImage(event.subject) }

            val reply = gachaImage.plus("\n").plus(pool.getArkDrawResultAsString(user, gachaResult))

            if (event is GroupMessageEvent) event.sender.at().plus("\n").plus(reply) else reply
        } else {
            (GachaUtil.overTimeMessage + "\n剩余积分: ${user.checkInPoint}").convertToChain()
        }
    }

    fun handleFreedomDraw(event: MessageEvent, user: CometUser, args: List<String>): MessageChain {
        return if (args[0].isNumeric()) {
            val gachaTime: Int = try {
                args[0].toInt()
            } catch (e: NumberFormatException) {
                return ArkNightCommand.getHelp().convertToChain()
            }

            return runBlocking { getGachaResult(event, user, gachaTime) }
        } else {
            ArkNightCommand.getHelp().convertToChain()
        }
    }

    fun configGachaPool(args: List<String>): MessageChain {
        return if (args.size == 1) {
            buildString {
                append("目前卡池: ${pool.name}\n")
                append("详细信息: ${pool.description}")

                val pools = GachaService.getPoolsByType<ArkNightPool>()

                append("\n\n卡池列表: ")

                pools.forEach {
                    append(it.name).append(",")
                }
            }.removeSuffix(",").toChain()
        } else {
            val poolName = args[1]
            val pools =
                GachaService.getPoolsByType<ArkNightPool>().parallelStream().filter { it.name == poolName }.findFirst()
            if (pools.isPresent) {
                pool = pools.get()
                "成功修改卡池为: ${pool.name}".toChain()
            } else {
                "找不到名为 $poolName 的卡池".toChain()
            }
        }
    }
}