package ren.natsuyuk1.comet.commands.service

import kotlinx.coroutines.launch
import moe.sdl.yabapi.data.search.results.UserResult
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.register
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.network.thirdparty.bilibili.SearchApi
import ren.natsuyuk1.comet.network.thirdparty.bilibili.UserApi
import ren.natsuyuk1.comet.network.thirdparty.bilibili.user.asReadable
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper

typealias PendingSearchResult = List<UserResult>

object BiliBiliService {
    val scope = ModuleScope("comet-bili-service")

    class BiliBiliUserQuerySession(
        contact: PlatformCommandSender,
        cometUser: CometUser?,
        private val pendingSearchResult: PendingSearchResult
    ) : Session(contact, cometUser) {
        init {
            val request = buildMessageWrapper {
                appendText("请选择你欲搜索的 UP 主 >", true)

                pendingSearchResult.take(5).forEachIndexed { index, userResult ->
                    appendText("${index + 1} >> ${userResult.uname} (${userResult.mid})", true)
                }

                appendText("请在下一条消息中回复选择的编号")
            }

            contact.sendMessage(request)
        }

        override fun handle(message: MessageWrapper) {
            val index = message.parseToString().toIntOrNull()

            if (index == null) {
                contact.sendMessage("请输入正确的编号!".toMessageWrapper())
            } else {
                val result = pendingSearchResult.getOrNull(index - 1)

                if (result == null) {
                    contact.sendMessage("请输入正确的编号!".toMessageWrapper())
                } else {
                    contact.sendMessage("🔍 正在查询用户 ${result.uname} 的信息...".toMessageWrapper())

                    scope.launch { queryUser(contact, result.mid!!) }
                }
            }
        }
    }

    suspend fun processUserSearch(subject: PlatformCommandSender, id: Int = 0, keyword: String = "") = scope.launch {
        if (id != 0) {
            queryUser(subject, id)
        } else {
            val searchResult = SearchApi.searchUser(keyword)?.data

            if (searchResult == null || searchResult.firstOrNull() !is UserResult) {
                subject.sendMessage("找不到你想要搜索的用户, 可能不存在哦".toMessageWrapper())
            } else {
                if (searchResult.size == 1) {
                    queryUser(subject, (searchResult as PendingSearchResult).first().mid!!)
                } else {
                    val user: CometUser? =
                        if (subject is User) CometUser.getUserOrCreate(subject.id, subject.platformName) else null
                    BiliBiliUserQuerySession(subject, user, searchResult as PendingSearchResult).register()
                }
            }
        }
    }

    suspend fun queryUser(subject: PlatformCommandSender, id: Int = 0) = scope.launch {
        val space = UserApi.getUserSpace(id)
        val card = UserApi.getUserCard(id)

        if (space == null || card == null) {
            subject.sendMessage("❌ 找不到对应 UID 的用户信息, 可能是 B 站问题?".toMessageWrapper())
            return@launch
        }

        subject.sendMessage(
            buildMessageWrapper {
                appendText("${space.name}")

                if (space.vip?.asReadable()?.isNotBlank() == true) {
                    appendText(" | ${space.vip?.asReadable()}")
                }

                appendLine()

                if (space.official?.asReadable()?.isNotBlank() == true) {
                    appendText("${space.official?.asReadable()}")
                }

                appendLine()

                appendText("签名 >> ${space.bio}", true)
                appendLine()
                appendText("粉丝 ${card.follower?.getBetterNumber()} | 获赞 ${card.like?.getBetterNumber()}", true)
                appendLine()
                appendText("\uD83D\uDD17 https://space.bilibili.com/${space.mid}")
            }
        )
    }
}
