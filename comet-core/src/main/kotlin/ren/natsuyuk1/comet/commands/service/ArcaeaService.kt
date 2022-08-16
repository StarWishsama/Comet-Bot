package ren.natsuyuk1.comet.commands.service

import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaClient
import ren.natsuyuk1.comet.objects.arcaea.ArcaeaUserData
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.message.MessageWrapper

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

    fun queryUserInfo(comet: Comet, subject: PlatformCommandSender, user: CometUser) {
        if (!ArcaeaUserData.isBound(user.id.value)) {
            subject.sendMessage("你还没有绑定过 Arcaea 账号, 记得先绑定哦~".toMessageWrapper())
        } else {
            val data = ArcaeaUserData.getUserArcaeaData(user.id.value) ?: kotlin.run {
                subject.sendMessage("你还没有绑定过 Arcaea 账号, 记得先绑定哦~".toMessageWrapper())
                return
            }

            comet.scope.launch {
                newSuspendedTransaction {
                    val userInfo = ArcaeaClient.queryUserInfo(data.userID)
                    if (userInfo == null) {
                        subject.sendMessage("❌ 查询用户数据失败".toMessageWrapper())
                    } else {
                        subject.sendMessage(userInfo.getMessageWrapper())
                    }
                }
            }
        }
    }
}
