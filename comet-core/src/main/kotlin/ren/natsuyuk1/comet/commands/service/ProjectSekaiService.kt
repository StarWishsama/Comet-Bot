package ren.natsuyuk1.comet.commands.service

import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.toMessageWrapper
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData as pjskUserData

object ProjectSekaiService {
    fun bindAccount(user: CometUser, userID: Long): MessageWrapper {
        if (pjskUserData.hasUserID(userID)) {
            return "该账号已被绑定，如果是你的账号被错误绑定请联系管理员".toMessageWrapper()
        }

        return if (pjskUserData.isBound(user.id.value)) {
            pjskUserData.updateID(user.id.value, userID)
            "已更改你绑定的账号 ID!".toMessageWrapper()
        } else {
            pjskUserData.createData(user.id.value, userID)
            "已成功绑定世界计划账号!".toMessageWrapper()
        }
    }

    suspend fun queryUserInfo(userID: Long): MessageWrapper {
        return cometClient.getUserInfo(userID).toMessageWrapper().apply {
            appendLine()
            appendLine()
        }
    }
}
