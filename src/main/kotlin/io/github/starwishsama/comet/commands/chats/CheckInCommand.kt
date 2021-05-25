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

import cn.hutool.core.util.RandomUtil

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.service.task.HitokotoUpdater
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

/**
 * [CheckInCommand]
 *
 * 签到命令, 可以获得积分
 *
 * @author StarWishsama
 * @author StivenDing
 */

class CheckInCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        return if (event is GroupMessageEvent) {
            if (user.isChecked()) {
                "你今天已经签到过了! 输入 /cx 可查询签到信息".toChain()
            } else {
                checkIn(event, user).convertToChain()
            }
        } else {
            "抱歉, 该命令仅供群聊使用".toChain()
        }
    }

    override fun getProps(): CommandProps =
        CommandProps("checkin", arrayListOf("签到", "qd"), "签到命令", "nbot.commands.checkin", UserLevel.USER)

    override fun getHelp(): String = "/qd 签到"

    private fun checkIn(msg: MessageEvent, user: BotUser): String {
        return run {
            val point = calculatePoint(user)
            val sender = msg.sender

            var extra = "\n连续签到 ${user.checkInTime} 天, 额外获得了 ${point[1]} 点积分~"
            if (user.checkInTime < 2 || point[1] == 0.0) {
                extra = ""
            }

            var text = "Hi ${sender.nameCardOrNick}, 签到成功!\n"
            if (msg is GroupMessageEvent) {
                user.checkInGroup = msg.group.id
            } else {
                user.checkInGroup = 0
            }

            text += if (point[0] + point[1] == 0.0) {
                "今天运气不佳, 没有积分"
            } else {
                "获得了 ${String.format("%.1f", point[0])} 点积分$extra\n目前积分数: ${String.format("%.1f", user.checkInPoint)}."
            }

            "$text\n${HitokotoUpdater.getHitokoto(false)}"
        }
    }

    /**
     * 计算签到所得积分
     *
     * @return 获取积分情况, [0] 为签到所得, [1] 为额外获得
     */
    private fun calculatePoint(user: BotUser): DoubleArray {
        // 计算签到时间
        val now = LocalDateTime.now()
        val duration = Duration.between(user.lastCheckInTime, now)
        if (duration.toDays() <= 1) {
            user.plusDay()
        } else {
            user.resetDay()
        }
        user.lastCheckInTime = now

        // 只取小数点后一位，将最大奖励点数限制到 3 倍
        val awardProp = 0.15 * (user.checkInTime - 1)
        // 使用随机数工具生成基础积分
        val basePoint = RandomUtil.randomDouble(0.0, 10.0, 1, RoundingMode.HALF_DOWN)
        // 连续签到的奖励积分
        val awardPoint = (if (awardProp < 3) {
            String.format("%.1f", awardProp * basePoint)
        } else {
            String.format("%.1f", 1.5 * basePoint)
        }).toDouble()

        user.addPoint(basePoint + awardPoint)

        return doubleArrayOf(basePoint, awardPoint)
    }

}