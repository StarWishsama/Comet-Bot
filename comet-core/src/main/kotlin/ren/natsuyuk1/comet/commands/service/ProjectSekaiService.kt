package ren.natsuyuk1.comet.commands.service

import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankSeasonInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.toMessageWrapper
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.service.image.ProjectSekaiImageService.drawEventInfo
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData as pjskData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData as pjskUserData
import ren.natsuyuk1.comet.service.ProjectSekaiManager as pjskHelper

object ProjectSekaiService {
    fun bindAccount(user: CometUser, userID: Long): MessageWrapper {
        return if (pjskUserData.isBound(user.id.value)) {
            pjskUserData.updateID(user.id.value, userID)
            "已更改你绑定的账号 ID!".toMessageWrapper()
        } else {
            pjskUserData.createData(user.id.value, userID)
            "已成功绑定世界计划账号!".toMessageWrapper()
        }
    }

    suspend fun queryUserEventInfo(user: CometUser, position: Int): MessageWrapper {
        val userData = pjskUserData.getUserPJSKData(user.id.value)
        val currentEventId = pjskData.getEventId() ?: return "获取当前活动信息失败, 请稍后再试".toMessageWrapper()

        if (position == 0 && userData == null) {
            return "你还没有绑定过世界计划账号, 使用 /pjsk bind -i [你的ID] 绑定".toMessageWrapper()
        }

        return when (pjskHelper.getCurrentEventStatus()) {
            SekaiEventStatus.ONGOING, SekaiEventStatus.END -> {
                if (position == 0 && userData != null) {
                    val cur = cometClient.getUserEventInfo(currentEventId, userData.userID)
                    if (SkikoHelper.isSkikoLoaded()) {
                        cur.drawEventInfo(currentEventId, userData)
                    } else {
                        cur.toMessageWrapper(userData, currentEventId)
                    }
                } else {
                    val cur = cometClient.getSpecificRankInfo(currentEventId, position)

                    if (SkikoHelper.isSkikoLoaded()) {
                        cur.drawEventInfo(currentEventId)
                    } else {
                        cur.toMessageWrapper(null, currentEventId)
                    }
                }
            }

            SekaiEventStatus.COUNTING -> {
                "活动数据统计中, 请耐心等待~".toMessageWrapper()
            }

            else -> {
                "获取当前活动信息失败, 请稍后再试".toMessageWrapper()
            }
        }
    }

    fun fetchPrediction(): MessageWrapper {
        val pred = pjskData.getCurrentPredictionInfo()
        val predUpdateTime = pjskData.getPredictionInfoTime()

        if (pred == null || predUpdateTime == null) {
            return "活动预测线信息暂未获取, 稍等片刻哦~".toMessageWrapper()
        }

        return pred.toMessageWrapper()
    }

    suspend fun queryUserInfo(user: CometUser): MessageWrapper {
        val userData = pjskUserData.getUserPJSKData(user.id.value)
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

        val userData = pjskUserData.getUserPJSKData(user.id.value)
            ?: return "你还没有绑定过世界计划账号, 使用 /pjsk bind -i [你的ID] 绑定".toMessageWrapper()

        return cometClient.getUserInfo(userData.userID).generateBest30()
    }
}
