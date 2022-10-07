package ren.natsuyuk1.comet.commands.service

import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asImage
import ren.natsuyuk1.comet.api.message.at
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaClient
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaSongInfo
import ren.natsuyuk1.comet.objects.arcaea.ArcaeaUserData
import ren.natsuyuk1.comet.service.image.ArcaeaImageService
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.brotli4j.BrotliDecompressor

object ArcaeaService {
    fun bindAccount(user: CometUser, userID: String): MessageWrapper {
        if (ArcaeaUserData.isBound(user.id.value)) {
            return transaction {
                ArcaeaUserData.updateID(user.id.value, userID)
                return@transaction "成功更新你的 Arcaea ID".toMessageWrapper()
            }
        } else {
            transaction {
                ArcaeaUserData.createData(user.id.value, userID)
            }

            return "成功绑定 Arcaea ID".toMessageWrapper()
        }
    }

    suspend fun queryUserInfo(comet: Comet, subject: PlatformCommandSender, user: CometUser) {
        if (!ArcaeaUserData.isBound(user.id.value)) {
            subject.sendMessage("你还没有绑定过 Arcaea 账号, 记得先绑定哦~".toMessageWrapper())
        } else {
            val data = ArcaeaUserData.getUserArcaeaData(user.id.value) ?: kotlin.run {
                subject.sendMessage("你还没有绑定过 Arcaea 账号, 记得先绑定哦~".toMessageWrapper())
                return
            }

            querySpecificUserInfo(comet, subject, data.userID)
        }
    }

    fun querySpecificUserInfo(comet: Comet, subject: PlatformCommandSender, id: String) {
        comet.scope.launch {
            if (!BrotliDecompressor.isUsable()) {
                subject.sendMessage("❌ 无法查询 Arcaea 数据, 缺少关键依赖.".toMessageWrapper())
            }

            newSuspendedTransaction {
                val userInfo = ArcaeaClient.queryUserInfo(id)
                if (userInfo == null) {
                    subject.sendMessage("❌ 查询用户数据失败".toMessageWrapper())
                } else {
                    subject.sendMessage(userInfo.getMessageWrapper())
                }
            }
        }
    }

    fun queryB38(
        comet: Comet,
        subject: PlatformCommandSender,
        sender: PlatformCommandSender,
        user: CometUser,
    ) = comet.scope.launch {
        val executedTime = Clock.System.now()

        if (!ArcaeaUserData.isBound(user.id.value)) {
            subject.sendMessage("❓ 你还没有绑定过 Arcaea 账号, 记得先绑定哦~".toMessageWrapper())
            return@launch
        }

        val data = ArcaeaUserData.getUserArcaeaData(user.id.value) ?: kotlin.run {
            subject.sendMessage("❓ 你还没有绑定过 Arcaea 账号, 记得先绑定哦~".toMessageWrapper())
            return@launch
        }

        if ((executedTime - data.best38Time.toInstant(TimeZone.currentSystemDefault())).inWholeMinutes < 15) {
            val userInfo = ArcaeaClient.queryUserInfo(data.userID)
            val best38 = json.decodeFromString<List<ArcaeaSongInfo>>(data.best38)

            if (userInfo == null) {
                subject.sendMessage("❌ 查询用户数据失败".toMessageWrapper())
                return@launch
            }

            val (_, b38Image) = ArcaeaImageService.drawB38(userInfo, best38)

            subject.sendMessage(
                buildMessageWrapper {
                    appendElement(sender.at())
                    appendElement(b38Image.asImage())
                }
            )

            return@launch
        }

        if (ArcaeaClient.getQueryUserCount() > 5) {
            subject.sendMessage("❌ 当前查询人数过多, 请稍后重试~".toMessageWrapper())
            return@launch
        }

        if (ArcaeaClient.isUserQuerying(user.id.value)) {
            subject.sendMessage("🏃再稍等一下, 结果马上出来~".toMessageWrapper())
            return@launch
        }

        subject.sendMessage("🔍 正在查询中, 通常会在三分钟内完成...".toMessageWrapper())

        val (userInfo, b30) = ArcaeaClient.queryUserB38(data.userID, user.id.value)

        if (userInfo == null) {
            subject.sendMessage(
                buildMessageWrapper {
                    appendText("❌ 查询用户 ${data.userID} 的信息失败")
                }
            )
            return@launch
        }

        val (b38, b38Image) = ArcaeaImageService.drawB38(userInfo, b30)

        data.best38 = json.encodeToString(b38)
        data.best38Time = executedTime.toLocalDateTime(TimeZone.currentSystemDefault())

        subject.sendMessage(
            buildMessageWrapper {
                appendElement(sender.at())
                appendElement(b38Image.asImage())
            }
        )
    }
}
