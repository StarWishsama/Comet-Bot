package ren.natsuyuk1.comet.commands.service

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.consts.client
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiHelper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.toMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper

object ProjectSekaiService {
    private var currentEventId = 1

    init {
        transaction {
            if (!ProjectSekaiData.all().empty()) {
                currentEventId = ProjectSekaiData.getCurrentEventInfo()!!.currentEventID
            } else {
                runBlocking { ProjectSekaiData.initData() }
            }
        }

        ProjectSekaiHelper.refreshCache()
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

    suspend fun lookupUserInfo(user: CometUser, position: Int): MessageWrapper {
        val userId = ProjectSekaiUserData.getProjectSekaiUserID(user.id.value)
            ?: return "你还没有绑定过世界计划账号, 使用 /pjsk bind -i [你的ID] 绑定".toMessageWrapper()

        return if (position == 0) {
            val currentInfo = client.getUserEventInfo(currentEventId, userId)
            currentInfo.toMessageWrapper(currentEventId)
        } else {
            client.getSpecificRankInfo(currentEventId, position).toMessageWrapper(currentEventId)
        }
    }

    suspend fun fetchPrediction(): MessageWrapper {
        return client.getRankPredictionInfo().toMessageWrapper()
    }
}
