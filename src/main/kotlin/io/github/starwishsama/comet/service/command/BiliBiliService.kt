/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.command

import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.SearchApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.UserApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.VideoApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.feed.toMessageWrapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.user.asReadable
import io.github.starwishsama.comet.api.thirdparty.bilibili.video.toMessageWrapper
import io.github.starwishsama.comet.commands.chats.BiliBiliCommand
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.managers.NetworkRequestManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.tasks.network.INetworkRequestTask
import io.github.starwishsama.comet.objects.tasks.network.NetworkRequestTask
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.NumberUtil.getBetterNumber
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import moe.sdl.yabapi.data.feed.FeedCardNode
import moe.sdl.yabapi.data.info.UserCardGetData
import moe.sdl.yabapi.data.info.UserSpace
import moe.sdl.yabapi.data.search.results.SearchResult
import moe.sdl.yabapi.data.search.results.UserResult
import moe.sdl.yabapi.data.video.VideoInfo
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText

object BiliBiliService {
    fun callSubscribeUser(
        user: CometUser,
        args: List<String>,
        event: MessageEvent
    ): MessageChain {
        val cmd = BiliBiliCommand

        if (args.size <= 1) {
            return cmd.getHelp().convertToChain()
        }

        if (!cmd.hasPermission(user, event)) {
            return LocalizationManager.getLocalizationText("message.no-permission").convertToChain()
        }

        return if (args[1].contains("|")) {
            val users = args[1].split("|")
            val id = if (event is GroupMessageEvent) event.group.id else args[2].toLong()

            subscribeUser(id, users).wrapToMessageChain().toMessageChain()
        } else {
            val id = if (event is GroupMessageEvent) event.group.id else args[2].toLong()
            subscribeUser(id, listOf(args[1])).wrapToMessageChain().toMessageChain()
        }
    }

    private fun subscribeUser(groupId: Long, subscribeTarget: List<String>): SubscribeResult {
        val cfg = GroupConfigManager.getConfigOrNew(groupId)

        val successList = mutableListOf<BiliBiliUser>()
        val failedList = mutableListOf<SubscribeResult.FailedUser>()

        subscribeTarget.forEach { target ->
            var userName = ""
            val uid: Int = when {
                target.isNumeric() -> {
                    userName = UserApi.getUserNameByMid(target.toInt())
                    target.toInt()
                }
                else -> {
                    val item: SearchResult = runBlocking { SearchApi.searchUser(keyword = target)?.data?.firstOrNull() } ?: return@forEach

                    if (item !is UserResult) {
                        return@forEach
                    }

                    val title = item.uname
                    if (title != null) userName = title
                    val mid = item.mid

                    if (mid != null) {
                        mid
                    } else {
                        failedList.add(
                            SubscribeResult.FailedUser(
                                target,
                                SubscribeResult.SubscribeFailedReason.NOT_FOUND
                            )
                        )
                        return@forEach
                    }
                }
            }

            val roomNumber: Int = runBlocking { UserApi.getUserSpace(uid)?.liveRoom?.roomId ?: -1 }

            if (cfg.biliSubscribers.stream().filter { it.id == uid.toString() }.findFirst().isPresent) {
                failedList.add(SubscribeResult.FailedUser(target, SubscribeResult.SubscribeFailedReason.EXISTS))
            } else {
                cfg.biliSubscribers.add(BiliBiliUser(uid.toString(), userName, roomNumber).also { successList.add(it) })
            }

            runBlocking {
                delay(500)
            }
        }

        return SubscribeResult(successList, failedList)
    }

    fun callUnsubscribeUser(args: List<String>, groupId: Long): MessageChain {
        val cmd = BiliBiliCommand

        return if (args.size > 1) {
            val cfg = GroupConfigManager.getConfigOrNew(groupId)

            if (args[1] == "all" || args[1] == "全部") {
                cfg.biliSubscribers.clear()
                return toChain("退订全部成功")
            }

            val item = if (args[1].isNumeric()) {
                val item = cfg.biliSubscribers.parallelStream().filter { it.id == args[1] }.findFirst()
                if (!item.isPresent) {
                    return "找不到你要退订的用户".toChain()
                }

                item.get()
            } else {
                val item = cfg.biliSubscribers.parallelStream().filter { it.userName == args[1] }.findFirst()
                if (!item.isPresent) {
                    return "找不到你要退订的用户".toChain()
                }

                item.get()
            }

            cfg.biliSubscribers.remove(item)
            "取消订阅用户 ${item.userName} 成功".toChain()
        } else {
            cmd.getHelp().convertToChain()
        }
    }

    fun getSubscribers(event: MessageEvent): MessageChain {
        if (event !is GroupMessageEvent) return "只能在群里查看订阅列表".toChain()
        val list = GroupConfigManager.getConfig(event.group.id)?.biliSubscribers

        if (list?.isNotEmpty() == true) {
            val subs = buildString {
                append("订阅列表:\n")
                list.forEach {
                    append(it.userName + " (${it.id}, ${it.roomID})\n")
                    trim()
                }
            }
            return toChain(subs)
        }
        return toChain("未订阅任何用户")
    }

    suspend fun searchUserInfo(userName: String, event: MessageEvent) {
        NetworkRequestManager.addTask(BiliBiliUserCheckTask(event.subject, userName))

        event.subject.sendMessage(event.message.quote() + "请稍等...")
    }

    suspend fun searchUserDynamic(userName: String, event: MessageEvent) {
        NetworkRequestManager.addTask(BiliBiliDynamicTask(event.subject, userName))

        event.subject.sendMessage(event.message.quote() + "请稍等...")
    }

    fun setParseVideo(groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfig(groupId) ?: return "请求的群聊不存在!".toChain()

        cfg.canParseBiliVideo = !cfg.canParseBiliVideo

        return "群聊视频解析已${if (cfg.canParseBiliVideo) "开启" else "关闭"}".toChain()
    }

    fun searchVideo(videoId: String, event: MessageEvent): MessageChain {
        val videoInfo: VideoInfo = runBlocking {
            return@runBlocking when {
                videoId.startsWith("BV") -> VideoApi.getVideoInfo(videoId)
                videoId.lowercase().startsWith("av") -> VideoApi.getVideoInfo(videoId.lowercase().replace("av", "").toInt())
                else -> null
            }
        } ?: return "请输入有效的 av/bv 号!".toChain()

        return runBlocking {
            val wrapper = videoInfo.toMessageWrapper()
            return@runBlocking if (!wrapper.isUsable()) {
                EmptyMessageChain
            } else {
                wrapper.toMessageChain(event.subject)
            }
        }
    }
}

data class SubscribeResult(
    val successUser: List<BiliBiliUser>,
    val failedList: List<FailedUser>
) {
    data class FailedUser(
        val input: String,
        val reason: SubscribeFailedReason
    )

    enum class SubscribeFailedReason(val reason: String) {
        NOT_FOUND("找不到对应用户"),
        EXISTS("已订阅过对应用户");
    }

    fun wrapToMessageChain(): MessageWrapper {
        return if (failedList.isEmpty()) {
            MessageWrapper().addText(
                CometUtil.sendMessageAsString(
                    if (successUser.size > 1) "订阅 ${successUser.size} 个用户成功" else "订阅 ${successUser.first().userName} 成功"
                )
            )
        } else {
            MessageWrapper().addText(
                CometUtil.sendMessageAsString(
                    if (failedList.size > 1) "订阅 ${successUser.size} 个成功, ${failedList.size} 个失败" else "订阅 ${failedList.first().input} 失败, 原因是 ${failedList.first().reason.reason}"
                )
            )
        }
    }
}

class BiliBiliUserCheckTask(
    override val content: Contact,
    override val param: String,
) : NetworkRequestTask(),
    INetworkRequestTask<Pair<UserCardGetData?, UserSpace?>> {
    override fun request(param: String): Pair<UserCardGetData?, UserSpace?> {
        val card = runBlocking {
            if (param.isNumeric()) {
                UserApi.getUserCard(param.toInt())
            } else {
                val searchResult = SearchApi.searchUser(param)?.data?.firstOrNull()

                if (searchResult !is UserResult) {
                    null
                } else {
                    searchResult.mid?.let { UserApi.getUserCard(it) }
                }
            }
        }

        val space = runBlocking {
            if (param.isNumeric()) {
                UserApi.getUserSpace(param.toInt())
            } else {
                val searchResult = SearchApi.searchUser(param)?.data?.firstOrNull()

                if (searchResult !is UserResult) {
                    null
                } else {
                    searchResult.mid?.let { UserApi.getUserSpace(it) }
                }
            }
        }

        return Pair(card, space)
    }

    @Suppress("UNCHECKED_CAST")
    override fun callback(result: Any?) {
        if (result is Pair<*, *> && result.first is UserCardGetData? && result.second is UserSpace?) {
            val (card, space) = result as Pair<UserCardGetData?, UserSpace?>

            val chain = if (space != null) {
                """
                ${space.name}${if (space.vip?.asReadable()?.isEmpty() == true) "" else " | ${space.vip?.asReadable()}"}
                ${space.official?.asReadable()}
                
                ${space.bio}
                                                                                       
                粉丝 ${card?.follower?.getBetterNumber()} | 获赞 ${card?.like?.getBetterNumber()}
                                         
                🔗 https://space.bilibili.com/${space.mid}                         
                """.trimIndent().toChain()
            } else {
                "找不到对应的B站用户".toChain()
            }

            runBlocking {
                content.sendMessage(chain)
            }
        } else {
            runBlocking {
                content.sendMessage("无法查询到对应B站用户信息".toChain())
            }
        }
    }

    override fun onFailure(t: Throwable?) {
        runBlocking {
            content.sendMessage("查询对应B站用户信息时出现错误".toChain())
        }
        daemonLogger.warning("查询哔哩哔哩用户信息时出现异常", t)
    }
}

class BiliBiliDynamicTask(
    override val content: Contact,
    override val param: String,
) : NetworkRequestTask(),
    INetworkRequestTask<FeedCardNode?> {
    override fun request(param: String): FeedCardNode? {
        return runBlocking {
            runCatching<FeedCardNode?> {
                val mid = if (param.isNumeric()) {
                    param.toIntOrNull()
                } else {
                    val searchResult = SearchApi.searchUser(param)?.data?.firstOrNull()

                    if (searchResult !is UserResult) {
                        null
                    } else {
                        searchResult.mid
                    }
                }

                return@runCatching mid?.let { DynamicApi.getUserDynamicTimeline(it)?.firstOrNull() }
            }.onFailure { e ->
                daemonLogger.error("获取动态失败", e)
            }.getOrNull()
        }
    }

    override fun callback(result: Any?) {
        if (result is FeedCardNode?) {
            runBlocking {
                if (result == null) {
                    content.sendMessage(CometUtil.sendMessageAsString("获取动态失败"))
                } else {
                    val wrapper = result.toMessageWrapper()
                    if (wrapper.isEmpty()) {
                        content.sendMessage(CometUtil.sendMessageAsString("获取动态失败, 可能是搜索的用户没发送过动态"))
                    } else {
                        content.sendMessage(PlainText("近期动态 > \n") + wrapper.toMessageChain(content))
                    }
                }
            }
        }
    }

    override fun onFailure(t: Throwable?) {
        runBlocking { content.sendMessage(CometUtil.sendMessageAsString("获取动态失败").toChain()) }
        daemonLogger.warning("获取动态失败", t)
    }
}
