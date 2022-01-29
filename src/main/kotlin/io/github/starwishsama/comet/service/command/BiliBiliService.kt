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

import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.SearchApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.UserApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertToWrapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserInfo
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserVideoInfo
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
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
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
            val uid: Long = when {
                target.isNumeric() -> {
                    userName = DynamicApi.getUserNameByMid(target.toLong())
                    target.toLong()
                }
                else -> {
                    val item = SearchApi.searchApiService.searchUser(keyword = target).execute().body()
                    val title = item?.data?.result?.get(0)?.userName
                    if (title != null) userName = title
                    val mid = item?.data?.result?.get(0)?.mid

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

            val roomNumber: Long =
                UserApi.userApiService.getUserInfo(uid).execute().body()?.data?.liveRoomInfo?.roomID ?: -1

            if (cfg.biliSubscribers.stream().filter { it.id.toLong() == uid }.findFirst().isPresent) {
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
    INetworkRequestTask<Pair<UserInfo?, UserVideoInfo?>> {
    override fun request(param: String): Pair<UserInfo?, UserVideoInfo?> {
        val userInfo: UserInfo? = if (param.isNumeric()) {
            UserApi.userApiService.getMemberInfoById(
                param.toLongOrNull() ?: return Pair(null, null)
            ).execute().body()
        } else {
            val searchResult =
                SearchApi.searchApiService.searchUser(keyword = param).execute().body()

            if (searchResult == null) {
                null
            } else {
                UserApi.userApiService.getMemberInfoById(searchResult.data.result[0].mid).execute()
                    .body()
            }
        }

        if (userInfo == null) {
            return Pair(null, null)
        }

        val recentVideos =
            UserApi.userApiService.getMemberVideoById(userInfo.data.card.mid).execute().body()

        return Pair(userInfo, recentVideos)
    }

    override fun callback(result: Any?) {
        if (result is Pair<*, *> && result.first is UserInfo? && result.second is UserVideoInfo?) {
            val item = result.first as UserInfo?

            val chain = if (item != null) {
                val recentVideos = result.second as UserVideoInfo?
                val card = item.data.card

                """
${card.name}${if (card.vipInfo.toString().isEmpty()) "" else " | ${card.vipInfo}"}
${card.officialVerifyInfo.toString().ifEmpty { "" }}

${card.sign}
                                                                        
粉丝 ${item.data.follower.getBetterNumber()} | 获赞 ${item.data.likeCount.getBetterNumber()}
                                    
最近投递视频: ${if (recentVideos == null) "没有投稿过视频" else recentVideos.data.list.videoList[0].toString()}    
                         
🔗 https://space.bilibili.com/${card.mid}                         
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
}

class BiliBiliDynamicTask(
    override val content: Contact,
    override val param: String,
) : NetworkRequestTask(),
    INetworkRequestTask<Dynamic?> {
    override fun request(param: String): Dynamic? {
        val userInfo: UserInfo = if (param.isNumeric()) {
            UserApi.userApiService.getMemberInfoById(
                param.toLongOrNull() ?: return null
            ).execute().body() ?: return null
        } else {
            val searchResult =
                SearchApi.searchApiService.searchUser(keyword = param).execute().body()

            if (searchResult == null) {
                return null
            } else {
                UserApi.userApiService.getMemberInfoById(searchResult.data.result[0].mid).execute()
                    .body() ?: return null
            }
        }

        return DynamicApi.getUserDynamicTimeline(userInfo.data.card.mid)
    }

    override fun callback(result: Any?) {
        if (result is Dynamic?) {
            runBlocking {
                if (result == null) {
                    content.sendMessage(CometUtil.sendMessageAsString("获取动态失败, 可能是搜索的用户不存在或者没发送过动态"))
                } else {
                    val wrapper = result.convertToWrapper()
                    if (wrapper.isEmpty()) {
                        content.sendMessage(CometUtil.sendMessageAsString("获取动态失败, 可能是搜索的用户没发送过动态"))
                    } else {
                        content.sendMessage(PlainText("近期动态 > \n") + wrapper.toMessageChain(content))
                    }
                }
            }
        }
    }

}
