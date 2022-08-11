package ren.natsuyuk1.comet.commands.service

import kotlinx.coroutines.runBlocking
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankSeasonInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.toMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import ren.natsuyuk1.comet.service.ProjectSekaiManager as pjskHelper

object ProjectSekaiService {
    private var currentEventId: Int?

    init {
        runBlocking {
            ProjectSekaiData.updateData()
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
            return "获取当前活动信息失败, 请稍后再试".toMessageWrapper()
        }

        return when (pjskHelper.getCurrentEventStatus()) {
            SekaiEventStatus.ONGOING -> {
                if (position == 0) {
                    val currentInfo = cometClient.getUserEventInfo(currentEventId!!, userId)
                    currentInfo.toMessageWrapper(userData, currentEventId!!)
                } else {
                    cometClient.getSpecificRankInfo(currentEventId!!, position)
                        .toMessageWrapper(userData, currentEventId!!)
                }
            }

            SekaiEventStatus.COUNTING -> {
                "活动数据统计中, 首先别急, 其次不要急".toMessageWrapper()
            }

            SekaiEventStatus.END -> {
                "当期活动已结束, 请等待下期活动".toMessageWrapper()
            }

            else -> {
                "获取当前活动信息失败, 请稍后再试".toMessageWrapper()
            }
        }
    }

    suspend fun fetchPrediction(): MessageWrapper {
        return cometClient.getRankPredictionInfo().toMessageWrapper()
    }

    suspend fun queryUserInfo(user: CometUser): MessageWrapper {
        val userData = ProjectSekaiUserData.getUserPJSKData(user.id.value)
            ?: return "你还没有绑定过世界计划账号, 使用 /pjsk bind -i [你的ID] 绑定".toMessageWrapper()

        val userId = userData.userID

        val rankSeason = ProjectSekaiManager.getLatestRankSeason() ?: return "查询排位数据时出现异常".toMessageWrapper()

        val rankInfo = cometClient.getRankSeasonInfo(userId, rankSeason)

        return cometClient.getUserInfo(userId).toMessageWrapper().apply {
            appendLine()
            appendLine()
            appendText(rankInfo.getRankInfo())
        }
    }

    suspend fun b30(user: CometUser): MessageWrapper {
        if (!SkikoHelper.isSkikoLoaded()) {
            return "Comet 的图像生成库还没加载, 生成不了图片捏".toMessageWrapper()
        }

        val userData = ProjectSekaiUserData.getUserPJSKData(user.id.value)
            ?: return "你还没有绑定过世界计划账号, 使用 /pjsk bind -i [你的ID] 绑定".toMessageWrapper()

        val userId = userData.userID

        return cometClient.getUserInfo(userId).generateBest30()
    }
}
