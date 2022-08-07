package ren.natsuyuk1.comet.commands.service

import kotlinx.coroutines.runBlocking
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiHelper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.toMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.message.MessageWrapper

object ProjectSekaiService {
    private var currentEventId: Int?

    init {
        runBlocking {
            ProjectSekaiData.updateData()
            ProjectSekaiHelper.refreshCache()
            currentEventId = ProjectSekaiData.getCurrentEventInfo()?.currentEventID
        }
    }

    fun bindAccount(user: CometUser, userID: Long): MessageWrapper {
        return if (ProjectSekaiUserData.isBound(user.id.value)) {
            ProjectSekaiUserData.updateID(user.id.value, userID)
            "已更改你绑定的账号 ID!".toMessageWrapper()
        } else {
            ProjectSekaiUserData.createData(user.id.value, userID)
            "已成功绑定世界计划账号!".toMessageWrapper()
        }
    }

    suspend fun queryUserEventInfo(user: CometUser, position: Int): MessageWrapper {
        val userData = ProjectSekaiUserData.getUserPJSKData(user.id.value)
            ?: return "你还没有绑定过世界计划账号, 使用 /pjsk bind -i [你的ID] 绑定".toMessageWrapper()

        val userId = userData.userID

        if (currentEventId == null) {
            ProjectSekaiData.updateData()
            currentEventId = ProjectSekaiData.getCurrentEventInfo()?.currentEventID!!
        }

        return if (position == 0) {
            val currentInfo = cometClient.getUserEventInfo(currentEventId!!, userId)
            currentInfo.toMessageWrapper(userData, currentEventId!!)
        } else {
            cometClient.getSpecificRankInfo(currentEventId!!, position).toMessageWrapper(userData, currentEventId!!)
        }
    }

    suspend fun fetchPrediction(): MessageWrapper {
        return cometClient.getRankPredictionInfo().toMessageWrapper()
    }

    suspend fun queryUserInfo(user: CometUser): MessageWrapper {
        val userData = ProjectSekaiUserData.getUserPJSKData(user.id.value)
            ?: return "你还没有绑定过世界计划账号, 使用 /pjsk bind -i [你的ID] 绑定".toMessageWrapper()

        val userId = userData.userID

        return cometClient.getUserInfo(userId).toMessageWrapper()
    }
}
