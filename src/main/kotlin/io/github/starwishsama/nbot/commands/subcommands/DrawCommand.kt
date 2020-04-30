package io.github.starwishsama.nbot.commands.subcommands

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.draw.ArkNightOperator
import io.github.starwishsama.nbot.objects.draw.PCRCharacter
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.DrawUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.stream.Collectors


class DrawCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ)){
            if (args.isNotEmpty()){
                when (args[0]){
                    "明日方舟", "舟游", "mrfz", "ak" -> {
                        return if (args.size == 2){
                            when (args[1]){
                                "十连" -> (BotUtil.getLocalMessage("msg.bot-prefix") + getArkDrawResult(user, 10)).toMessage().asMessageChain()
                                "单抽" -> (BotUtil.getLocalMessage("msg.bot-prefix") + getArkDrawResult(user, 1)).toMessage().asMessageChain()
                                else -> {
                                    if (StringUtils.isNumeric(args[1])){
                                        (BotUtil.getLocalMessage("msg.bot-prefix") + getArkDrawResult(user, args[1].toInt())).toMessage().asMessageChain()
                                    } else {
                                        getHelp().toMessage().asMessageChain()
                                    }
                                }
                            }
                        } else {
                            getHelp().toMessage().asMessageChain()
                        }
                    }
                    "公主连结", "pcr", "gzlj" -> {
                        return if (args.size == 2){
                            when (args[1]){
                                "十连" -> (BotUtil.getLocalMessage("msg.bot-prefix") + getPCRResult(user, 10)).toMessage().asMessageChain()
                                "单抽" -> (BotUtil.getLocalMessage("msg.bot-prefix") + getPCRResult(user, 1)).toMessage().asMessageChain()
                                else -> {
                                    if (StringUtils.isNumeric(args[1])){
                                        (BotUtil.getLocalMessage("msg.bot-prefix") + getPCRResult(user, args[1].toInt())).toMessage().asMessageChain()
                                    } else {
                                        getHelp().toMessage().asMessageChain()
                                    }
                                }
                            }
                        } else {
                            return getHelp().toMessage().asMessageChain()
                        }
                    }
                    else -> return getHelp().toMessage().asMessageChain()
                }
            } else {
                return getHelp().toMessage().asMessageChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("draw", arrayListOf("ck", "抽卡"), "nbot.commands.draw", UserLevel.USER)

    override fun getHelp(): String = """
         ============ 命令帮助 ============
         /ck mrfz [十连/单抽/次数] 明日方舟抽卡
         /ck pcr [十连/单抽/次数] 公主连结抽卡
    """.trimIndent()

    private fun getArkDrawResult(user: BotUser, time: Int): String {
        val result = LinkedList<ArkNightOperator>()
        var r6Time = 0
        if (time == 1){
            return if (user.commandTime >= 1 || user.compareLevel(UserLevel.ADMIN)) {
                user.decreaseTime()
                val (name, _, rare) = DrawUtil.drawAr()
                name + " " + getStar(rare)
            } else {
                "今日抽卡次数已达上限, 别抽卡上头了"
            }
        } else if (time == 10){
            return if (user.commandTime >= 10 || user.compareLevel(UserLevel.ADMIN)) {
                result.addAll(DrawUtil.tenTimeDrawAr())
                user.decreaseTime(10)
                val sb = StringBuilder("十连结果:\n")
                for ((name, _, rare) in result) {
                    sb.append(name).append(" ").append(getStar(rare)).append(" ")
                }
                sb.toString().trim()
            } else {
                "今日抽卡次数已达上限, 别抽卡上头了"
            }
        } else {
            if (user.commandTime >= time || user.compareLevel(UserLevel.ADMIN) && time <= 10000) {
                for (i in 0 until time) {
                    if (user.commandTime >= 1 || user.compareLevel(UserLevel.ADMIN)) {
                        user.decreaseTime(1)
                        if (i == 50) {
                            r6Time = RandomUtil.randomInt(51, time - 1)
                        }

                        if (r6Time != 0 && i == r6Time) {
                            result.add(DrawUtil.getOperator(6))
                        } else {
                            result.add(DrawUtil.drawAr())
                        }
                    } else {
                        break
                    }
                }
                val r6Char = result.stream().filter { it.rare == 6 }.collect(Collectors.toList())
                val r6Text = StringBuilder()
                r6Char.forEach { r6Text.append("${it.name} ${getStar(it.rare)} ")}

                return "抽卡结果:\n" +
                        "抽卡次数: ${result.size}\n" +
                        "六星: ${r6Text.toString().trim()}\n" +
                        "五星个数: ${result.stream().filter { it.rare == 5 }.count()}\n" +
                        "四星个数: ${result.stream().filter { it.rare == 4 }.count()}\n" +
                        "三星个数: ${result.stream().filter { it.rare == 3 }.count()}"
            } else {
               return "你要抽卡的次数大于你剩余的抽卡次数"
            }
        }
    }

    private fun getPCRResult(user: BotUser, time: Int): String{
        return if (time == 10) {
            if (user.commandTime >= 10) {
                user.decreaseTime(10)
                val ops: List<PCRCharacter> = DrawUtil.tenTimesDrawPCR()
                val sb = java.lang.StringBuilder("十连结果:\n")
                for ((name, star) in ops) {
                    sb.append(name).append(" ").append(getStar(star)).append(" ")
                }
                sb.toString().trim { it <= ' ' }
            } else {
                "今日抽卡次数已达上限, 别抽卡上头了"
            }
        } else if (time == 1){
            if (user.commandTime >= 1) {
                user.decreaseTime()
                val (name, star) = DrawUtil.drawPCR()
                name + " " + getStar(star)
            } else {
                "今日抽卡次数已达上限, 别抽卡上头了"
            }
        } else {
            if (user.commandTime >= time) {
                val startTime = System.currentTimeMillis()
                val ops: MutableList<PCRCharacter> = LinkedList()

                for (i in 0 until time) {
                    if (user.commandTime > 0) {
                        user.decreaseTime()
                        if (i % 10 == 0){
                            ops.add(DrawUtil.getCharacter(2))
                        } else {
                            ops.add(DrawUtil.drawPCR())
                        }
                    } else {
                        break
                    }
                }

                val r3s = ops.stream().filter { (_, star) -> star == 3 }.collect(Collectors.toList())

                val sb = StringBuilder()
                for ((name) in r3s) {
                    sb.append(name).append(" ")
                }

                return """
            抽卡次数: ${ops.size}
            三星角色: ${if (sb.toString().trim { it <= ' ' }.isEmpty()) "未抽到" else sb.toString().trim { it <= ' ' }}
            二星角色数: ${ops.stream().filter { (_, star) -> star == 2 }.count()}
            一星角色数: ${ops.stream().filter { (_, star) -> star == 1 }.count()}
            耗时: ${System.currentTimeMillis() - startTime}ms
            """.trimIndent()
            } else {
                return "你要抽卡的次数大于你的抽卡次数"
            }
        }
    }

    private fun getStar(rare: Int): String {
        val sb = StringBuilder("★")
        for (i in 1 until rare) {
            sb.append("★")
        }
        return sb.toString()
    }
}