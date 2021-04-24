package io.github.starwishsama.comet.service.compatibility

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.UserApi
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.service.compatibility.data.OldGroupConfig
import io.github.starwishsama.comet.utils.copyAndRename
import io.github.starwishsama.comet.utils.json.isUsable
import java.io.File
import kotlin.streams.toList

/**
 * [CompatibilityService]
 *
 * 负责转换破坏性更新时的数据类.
 *
 * @see [BotUser]
 * @see [io.github.starwishsama.comet.objects.config.CometConfig]
 */
object CompatibilityService {
    fun checkConfigFile(cfgFile: File): Boolean {
        val tree = mapper.readTree(cfgFile)
        try {
            if (tree["bili_sub"].isUsable()) {
                val test = tree["bili_sub"] as List<*>

                return test.first() !is Long
            }
        } catch (ignored: Exception) {
        }

        val cfg: OldGroupConfig

        try {
            daemonLogger.log(HinaLogLevel.Info, "已检测到旧版本配置 ${cfgFile.name}, 正在迁移...", prefix = "兼容性")

            cfg = mapper.readValue(cfgFile, OldGroupConfig::class.java)
        } catch (e: Exception) {
            daemonLogger.log(HinaLogLevel.Warn, "转换旧版本配置 ${cfgFile.name} 数据失败!", e, prefix = "兼容性")
            return false
        }
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

    /**
     * 转换旧版本用户数据
     *
     * @param userData 用户数据文件
     * @return 转换是否成功
     */
    fun checkUserData(userData: File): Boolean {
        try {
            mapper.readValue<Map<Long, BotUser>>(userData)
            return true
        } catch (ignored: Exception) {
        }

        try {
            userData.copyAndRename("users.json.old")
            val old = mapper.readValue<List<BotUser>>(userData)
            old.forEach {
                val actual = handleDuplication(old, it)
                BotVariables.users[actual.id] = actual
            }

            return true
        } catch (e: Exception) {
            daemonLogger.log(HinaLogLevel.Warn, "转换旧版本用户数据失败!", e, prefix = "兼容性")
        }

        return false
    }

    private fun handleDuplication(users: List<BotUser>, current: BotUser): BotUser {
        val duplicatedUser = users.parallelStream().filter { it.id == current.id }.toList()

        if (duplicatedUser.isEmpty()) {
            return current
        }
        for (du in duplicatedUser) {
            if (du.checkInPoint > current.checkInPoint || current.checkInPoint == 0.0) {
                return du
            }
        }

        return current
    }
}