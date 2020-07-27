package io.github.starwishsama.comet.commands.subcommands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.tasks.HitokotoUpdater
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.toMsgChain
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

class CheckInCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(event.sender.id) && event is GroupMessageEvent) {
            return if (BotUtil.isChecked(user)) {
                BotUtil.sendMsgPrefix("你今天已经签到过了! 输入 /cx 可查询签到信息").toMsgChain()
            } else {
                checkIn(event.sender, event, user).toMsgChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("checkin", arrayListOf("签到", "qd"), "签到命令", "nbot.commands.checkin", UserLevel.USER)

    override fun getHelp(): String = ""

    private fun checkIn(sender: User, msg: MessageEvent, user: BotUser): String {
        return run {
            val point = calculatePoint(user)
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
                "获得了 ${point[0]} 点积分$extra\n目前积分数: ${String.format("%.1f", user.checkInPoint)}."
            }

            text + HitokotoUpdater.getHitokoto()
        }
    }

    private fun calculatePoint(user: BotUser): DoubleArray {
        val now = LocalDateTime.now()
        // 计算连续签到次数，此处用了 Date 这个废弃的类，应换为 Calendar，too lazy to do so.
        // 已经更换为 Java 8 的全新日期 API :)
        val duration = Duration.between(user.lastCheckInTime, now)
        if (duration.toDays() <= 1) {
            user.plusDay()
        } else {
            user.resetDay()
        }
        user.lastCheckInTime = now

        // 只取小数点后一位，将最大奖励点数限制到 3 倍
        // 你爆率写这么高干锤子
        val awardProp = 0.15 * (user.checkInTime - 1)
        // 在吗 为什么不用新的随机数工具 自带的爆率好像有点高
        val basePoint = RandomUtil.randomDouble(0.0, 10.0, 1, RoundingMode.HALF_DOWN)
        val awardPoint = (if (awardProp < 3) {
            String.format("%.1f", awardProp * basePoint).toDouble()
        } else {
            1.5 * basePoint
        }).toDouble()
        user.addPoint(basePoint + awardPoint)
        return doubleArrayOf(basePoint, awardPoint)
    }

}