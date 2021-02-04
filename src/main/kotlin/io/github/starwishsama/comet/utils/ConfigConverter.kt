package io.github.starwishsama.comet.utils

import com.google.gson.JsonSyntaxException
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.nullableGson
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.config.BiliConfigTestObject
import io.github.starwishsama.comet.objects.config.OldGroupConfig
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import java.io.File

object ConfigConverter {
    fun convertOldGroupConfig(cfgFile: File): Boolean {
        lateinit var cfg: OldGroupConfig
        try {
            val context = cfgFile.getContext()
            nullableGson.fromJson(context, BiliConfigTestObject::class.java)
            cfg = nullableGson.fromJson(context, OldGroupConfig::class.java)
        } catch (e: JsonSyntaxException) {
            return false
        }

        daemonLogger.info("已检测到旧版本配置文件, 正在迁移...")

        val new = PerGroupConfig(cfg.id)

        val biliUsers = mutableListOf<BiliBiliUser>()

        cfg.biliSubscribers.forEach {
            biliUsers.add(BiliBiliUser(it.toString(), BiliBiliMainApi.getUserNameByMid(it), LiveApi.getRoomIDByUID(it)))
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
            this.doRepeat = cfg.doRepeat
            this.groupFilterWords.addAll(cfg.groupFilterWords)
            this.disabledCommands.addAll(cfg.disabledCommands)
        }

        GroupConfigManager.addConfig(new)

        return true
    }
}