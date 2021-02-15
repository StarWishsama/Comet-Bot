package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.BotVariables.nullableGson
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.UserApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.config.OldGroupConfig
import io.github.starwishsama.comet.objects.config.OldVersionTestObject
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import java.io.File

object ConfigConverter {
    fun convertOldGroupConfig(cfgFile: File): Boolean {
        val context = cfgFile.getContext()
        try {
            gson.fromJson(context, OldVersionTestObject::class.java)
            return false
        } catch (ignored: Exception) {}

        daemonLogger.info("已检测到旧版本配置文件, 正在迁移...")

        val cfg = nullableGson.fromJson(context, OldGroupConfig::class.java)
        val new = PerGroupConfig(cfg.id)

        val biliUsers = mutableListOf<BiliBiliUser>()

        cfg.biliSubscribers.forEach {
            biliUsers.add(
                BiliBiliUser(
                    it.toString(),
                    DynamicApi.getUserNameByMid(it),
                    UserApi.userApiService.getMemberInfoById(it)
                        .execute()
                        .body()?.data?.liveRoomInfo?.roomId ?: -1
                )
            )
        }

        new.apply {
            this.keyWordReply.addAll(cfg.keyWordReply)
            this.youtubePushEnabled = cfg.youtubePushEnabled
            this.twitterSubscribers.addAll(cfg.twitterSubscribers)
            this.biliSubscribers.addAll(biliUsers)
            this.autoAccept = cfg.autoAccept
            this.helpers = cfg.helpers
            this.twitterPushEnabled = cfg.twitterPushEnabled
            this.biliPushEnabled = cfg.biliPushEnabled
            this.youtubeSubscribers.addAll(cfg.youtubeSubscribers)
            this.canRepeat = cfg.doRepeat
            this.groupFilterWords.addAll(cfg.groupFilterWords)
            this.disabledCommands.addAll(cfg.disabledCommands)
        }

        GroupConfigManager.addConfig(new)

        return true
    }
}