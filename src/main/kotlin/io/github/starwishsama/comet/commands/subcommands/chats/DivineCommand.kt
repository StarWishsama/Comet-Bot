package io.github.starwishsama.comet.commands.subcommands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.RandomResult
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.BotUtil.getRestString
import io.github.starwishsama.comet.utils.BotUtil.hasNoCoolDown
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

class DivineCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (hasNoCoolDown(event.sender.id)) {
            val underCover = getResultFromList(BotVariables.underCovers, event.sender.id)
            return if (args.isNotEmpty()) {
                if (underCover == null) {
                    if (user.commandTime > 0 || user.level != UserLevel.USER) {
                        val randomEventName = args.getRestString(0)
                        if (randomEventName.isNotBlank() && randomEventName.length < 30) {
                            val result = RandomResult(-1000, RandomUtil.randomDouble(0.0, 1.0), randomEventName)
                            user.decreaseTime()
                            RandomResult.getChance(result).convertToChain()
                        } else {
                            BotUtil.sendMessage("请检查需要占卜的字符是否超过上限或为空!")
                        }
                    } else {
                        BotUtil.sendMessage("今日命令条数已达上限, 请等待条数自动恢复哦~\n命令条数现在每小时会恢复100次, 封顶1000次")
                    }
                } else {
                    BotVariables.underCovers.minusAssign(underCover)
                    RandomResult.getChance(underCover).convertToChain()
                }
            } else {
                return getHelp().convertToChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("divine", arrayListOf("zb", "占卜"), "占卜命令", "nbot.commands.divine", UserLevel.USER)

    override fun getHelp(): String = """
         ======= 命令帮助 =======
         /zb [占卜内容] 占卜
    """.trimIndent()

    private fun getResultFromList(results: List<RandomResult>, id: Long): RandomResult? {
        if (results.isNotEmpty()) {
            for (result in results) {
                if (result.id == id) {
                    return result
                }
            }
        }
        return null
    }
}
