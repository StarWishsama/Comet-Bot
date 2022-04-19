package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.CallbackCommand
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.genshin.gacha.data.item.Item
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemPool
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.pool.GachaPool
import io.github.starwishsama.comet.genshin.gacha.pool.GachaPoolManager
import io.github.starwishsama.comet.genshin.gacha.pool.WeaponPool
import io.github.starwishsama.comet.genshin.utils.DrawHelper
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.utils.uploadAsImage
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.sourceMessage

object GenshinGachaCommand : ChatCommand, ConversationCommand, CallbackCommand {

    private fun String.autoReply(event: MessageEvent): MessageChain {
        return event.message.quote().plus(this)
    }

    private fun MessageChain.autoReply(event: MessageEvent): MessageChain {
        return event.message.quote().plus(this)
    }


    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (event is GroupMessageEvent) {
            val pools = GachaPoolManager.getGachaPools()
            val validator: () -> GachaState = {
                val poolId = user.genshinGachaPool
                if (pools.size > poolId && poolId >= 0) {
                    GachaState.ACCEPT(pools[poolId], poolId)
                } else {
                    GachaState.REJECT("当前选定祈愿 ID: $poolId 不存在，请重新使用 /genshin pool [ID] 选定祈愿。查询祈愿 ID 输入 /genshin poolList".autoReply(event))
                }
            }
            when (args.size) {
                0 -> {
                    return when (val state = validator()) {
                        is GachaState.ACCEPT -> {
                            val results = state.gachaPool.tenGacha(user.id)
                            var img = DrawHelper().drawTenGachaImg(user.id, results, GachaPoolManager.getAllResults(user.id)).uploadAsImage(event.subject).toMessageChain().autoReply(event)
                            if (results.any{ it.item.itemStar == ItemStar.FIVE } || results.count { it.item.itemStar == ItemStar.FOUR } >= 4) {
                                img = img.plus("")
                            }
                            img
                        }
                        is GachaState.REJECT -> state.reason
                    }
                }
                1 -> when (args[0]) {
                    "help" -> getHelp().autoReply(event)
                    "1" -> {
                        return when (val state = validator()) {
                            is GachaState.ACCEPT -> {
                                val result = state.gachaPool.gacha(user.id)
                                var img = DrawHelper().drawGachaImg(user.id, result, GachaPoolManager.getAllResults(user.id)).uploadAsImage(event.subject).toMessageChain().autoReply(event)
                                if (result.item.itemStar == ItemStar.FIVE) img = img.plus("")
                                img
                            }
                            is GachaState.REJECT -> state.reason
                        }
                    }
                    "result" -> {
                        return buildString {
                            val recordPools = GachaPoolManager.getAllResults(user.id)
                            appendLine("所有祈愿 ── 期望记录仅显示 4 - 5 星：")
                            var i = 0
                            val compactResult = hashMapOf<Int, Int>()
                            recordPools.forEach { item ->
                                if (item.item.itemStar.star >= 4) {
                                    val count = compactResult.getOrDefault(item.item.id, 0)
                                    compactResult[item.item.id] = count + 1
                                }
                            }
                            val total = recordPools.size
                            val compactItemResult = compactResult.entries.fold(hashMapOf<Item, Int>()) {acc, (id, count) -> acc[ItemPool.getItemFromId(id)] = count; acc}
                            compactItemResult.entries.sortedByDescending { it.key.itemStar.star }.forEach { (item, count) ->
                                if (i != 0 && i % 5 == 0) appendLine()
                                append("${item.itemStar.star}* ${item.itemName} x${count}       ")
                                i++
                            }
                            val fiveStar = compactItemResult.entries.fold(0) {acc, entry -> if (entry.key.itemStar == ItemStar.FIVE) acc + entry.value else acc}
                            appendLine("\n\n─────────────\n")
                            appendLine("总祈愿数: $total${if (fiveStar > 0) ", 平均 5* 祈愿数: ${(total.toDouble() / fiveStar.toDouble()).format(2)}" else ""}, 折合原石: ${total*160}，折合人民币约：${total*12}")
                        }.autoReply(event)
                    }
                    "reset" -> {
                        return when (val state = validator()) {
                            is GachaState.ACCEPT -> {
                                state.gachaPool.reset(user.id)
                                "已经重置当前 ${state.gachaPool.getPoolType().name}，ID: ${state.id} 的祈愿状态".autoReply(event)
                            }
                            is GachaState.REJECT -> state.reason.autoReply(event)
                        }
                    }
                    "resetAll" -> {
                        GachaPoolManager.resetAllPool(user.id)
                        return "已重置所有祈愿状态".autoReply(event)
                    }
                    "poolList" -> {
                        return buildString {
                            pools.forEachIndexed { index, pool ->
                                appendLine("${pool.getPoolType().name}，ID: $index")
                                pool.upFiveStarList.takeIf { it.isNotEmpty() }?.let {
                                    appendLine("UP 5*: ${buildString { it.forEach { append(" ${it.itemName}") } }}")
                                }
                                pool.upFourStarList.takeIf { it.isNotEmpty() }?.let {
                                    appendLine("UP 4*: ${buildString { it.forEach { append(" ${it.itemName}") } }}")
                                }
                            }
                        }.autoReply(event)
                    }
                    "destiny" -> {
                        return when (val state = validator()) {
                            is GachaState.ACCEPT -> {
    //                            state.gachaPool.gacha(user.id).toString().toMessageChain()
                                val pool = state.gachaPool
                                if (pool is WeaponPool) {
                                    val destinyInfo = pool.getDestinyInfo(user.id)
                                    buildString {
                                        if (destinyInfo.first != 0) {
                                            appendLine("当前定轨武器为「${ItemPool.getItemFromId(destinyInfo.first)}」，「命定值 ${destinyInfo.second} / 2」")
                                            appendLine("切换武器将会导致命定值清零")
                                            appendLine("\n─────────────\n")
                                        }
                                        appendLine("请在 1 分钟内输入你想要在祈愿中「神铸定轨」的武器序号")
                                        appendLine("\n─────────────\n")
                                        state.gachaPool.upFiveStarList.forEachIndexed { index, item ->
                                            appendLine("$index - 「$item」")
                                        }
                                        SessionHandler.insertSession(
                                            DestinySession(
                                                SessionTarget(targetId = event.sender.id),
                                                state.gachaPool.upFiveStarList.foldIndexed(hashMapOf()) { index, acc, item -> acc[index.toString()] = item.id; acc },
                                                pool
                                            )
                                        )
                                    }.autoReply(event)
                                } else {
                                    "当前选择的不是武器祈愿，只有武器祈愿可以使用与查看「神铸定轨」机制".autoReply(event)
                                }
                            }
                            is GachaState.REJECT -> state.reason
                        }
                    }
                    "destinyVal" -> {
                        return when (val state = validator()) {
                            is GachaState.ACCEPT -> {
    //                            state.gachaPool.gacha(user.id).toString().toMessageChain()
                                val pool = state.gachaPool
                                if (pool is WeaponPool) {
                                    val destinyInfo = pool.getDestinyInfo(user.id)
                                    if (destinyInfo.first != 0) {
                                        "当前定轨武器为「${ItemPool.getItemFromId(destinyInfo.first)}」，「命定值 ${destinyInfo.second} / 2」".autoReply(event)
                                    } else {
                                        "当前没有使用「神铸定轨」机制对任何武器进行定轨".autoReply(event)
                                    }
                                } else {
                                    "当前选择的不是武器祈愿，只有武器祈愿可以使用与查看「神铸定轨」机制".autoReply(event)
                                }
                            }
                            is GachaState.REJECT -> state.reason
                        }
                    }

                }
                2 -> when (args[0]) {
                    "pool" -> {
                        return try {
                            val poolId = args[1].toInt()
                            if (pools.size > poolId && poolId >= 0) {
                                user.genshinGachaPool = poolId
                                "成功设置当前祈愿 ID 为 $poolId".autoReply(event)
                            } else {
                                "输入的祈愿 ID 不存在，查询祈愿 ID 输入 /genshin poolList".autoReply(event)
                            }
                        } catch (e: Exception) {
                            getHelp().autoReply(event)
                        }
                    }
                }
            }
        }
        return getHelp().autoReply(event)
    }


    override val props: CommandProps =
        CommandProps(
            "genshin",
            arrayListOf("g", "gen", "祈愿", "prey"),
            "原神祈愿模拟器",
            UserLevel.USER,
        )

    override fun getHelp(): String = """
         /genshin 十连
         /genshin 1 单抽
         /genshin reset 重置当前选择祈愿
         /genshin resetAll 重置所有祈愿
         /genshin poolList 查看当前所有祈愿
         /genshin pool [ID] 选定祈愿
         /genshin destiny 对当前祈愿进行「神铸定轨」设置
         /genshin destinyVal 查看当前「命定值」
         /genshin result 查看当前祈愿的所有记录
    """.trimIndent()

    @Suppress("NAME_SHADOWING")
    override suspend fun handle(event: MessageEvent, user: CometUser, session: Session) {
        if (session.isExpired(60)) {
            SessionHandler.removeSession(session)
        }
        if (session is DestinySession) {
            if (session.destinyMap.containsKey(event.message.contentToString())) {
                val itemId = session.destinyMap[event.message.contentToString()] ?: 0
                session.pool.setDestiny(user.id, itemId)
                event.subject.sendMessage("成功定轨武器「${ItemPool.getItemFromId(itemId)}」".autoReply(event))
            } else {
                event.subject.sendMessage("输入错误，请重新通过 /genshin destiny 进入定轨设置".autoReply(event))
            }
            SessionHandler.removeSession(session)
        }
    }

    override fun handleReceipt(receipt: MessageReceipt<Contact>) {
        if (!receipt.sourceMessage.contentToString().contains("!#!")) receipt.recallIn(1000*60)
    }

    fun Double.format(digits: Int) = "%.${digits}f".format(this)

}


sealed class GachaState {
    data class REJECT(val reason: MessageChain): GachaState()
    data class ACCEPT(val gachaPool: GachaPool, val id: Int): GachaState()
}

open class DestinySession(override val target: SessionTarget, val destinyMap: Map<String, Int>, val pool: WeaponPool) :
    Session(target, GenshinGachaCommand, false) {

}