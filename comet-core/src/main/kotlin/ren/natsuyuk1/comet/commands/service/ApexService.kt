package ren.natsuyuk1.comet.commands.service

import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.ApexLegendAPI.fetchUserID
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.ApexLegendAPI.fetchUserInfo
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.ApexLegendAPI.fetchUserInfoByName
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.data.toMessageWrapper
import ren.natsuyuk1.comet.objects.apex.ApexLegendData
import ren.natsuyuk1.comet.util.toMessageWrapper

object ApexService {
    private fun isUsable() = CometGlobalConfig.data.apexLegendToken.isNotEmpty()

    suspend fun bindAccount(user: CometUser, username: String): MessageWrapper {
        if (!isUsable()) {
            return "Comet 未注册 Apex Legends API, 无法查询".toMessageWrapper()
        }

        if (ApexLegendData.isBound(user.id.value)) {
            return "你已经绑定过账号了捏".toMessageWrapper()
        } else {
            val userID = cometClient.fetchUserID(username, "PC")

            if (userID.error != null) {
                return "查询失败, 该账号不存在或是 API 故障. Steam 玩家请使用 Origin 的用户名".toMessageWrapper()
            }

            ApexLegendData.createData(user.id.value, userID.uid)
            return "绑定账号 ${userID.name} 成功".toMessageWrapper()
        }
    }

    suspend fun queryUserInfo(user: CometUser, username: String = ""): MessageWrapper {
        if (!isUsable()) {
            return "Comet 未注册 Apex Legends API, 无法查询".toMessageWrapper()
        }

        if (username.isBlank()) {
            if (ApexLegendData.isBound(user.id.value)) {
                val data = ApexLegendData.getUserApexData(user.id.value) ?: return "你还没有绑定过账号".toMessageWrapper()

                return cometClient.fetchUserInfo(data.userID, "PC").toMessageWrapper()
            } else {
                return "你还没有绑定过账号".toMessageWrapper()
            }
        } else {
            val data = cometClient.fetchUserInfoByName(username, "PC")

            if (data.error != null) {
                return "查询失败, 该账号不存在或是 API 故障. Steam 玩家请使用 Origin 的用户名".toMessageWrapper()
            }

            return data.toMessageWrapper()
        }
    }
}
