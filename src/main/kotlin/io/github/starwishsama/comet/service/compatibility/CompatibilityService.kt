/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.compatibility

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.service.compatibility.data.OldGroupConfig
import io.github.starwishsama.comet.utils.copyAndRename
import io.github.starwishsama.comet.utils.json.isUsable
import java.io.File
import java.util.stream.Collectors

/**
 * [CompatibilityService]
 *
 * 负责转换破坏性更新时的数据类.
 *
 * @see [CometUser]
 * @see [PerGroupConfig]
 */
object CompatibilityService {
    fun checkConfigFile(cfgFile: File): Boolean {
        val tree = mapper.readTree(cfgFile)
        try {
            return tree["version"].isUsable()
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
                    LiveApi.getLiveInfo(it)?.data?.roomId ?: -1
                )
            )
        }

        new.apply {
            this.keyWordReply.addAll(cfg.keyWordReply)
            this.twitterSubscribers.addAll(cfg.twitterSubscribers)
            this.biliSubscribers.addAll(biliUsers)
            this.autoAccept = cfg.autoAccept
            this.helpers = cfg.helpers
            this.twitterPushEnabled = cfg.twitterPushEnabled
            this.biliPushEnabled = cfg.biliPushEnabled
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
            mapper.readValue<Map<Long, CometUser>>(userData)
            return true
        } catch (ignored: Exception) {
        }

        try {
            userData.copyAndRename("users.json.old")
            val old = mapper.readValue<List<CometUser>>(userData)
            old.forEach {
                val actual = handleDuplication(old, it)
                CometVariables.cometUsers[actual.id] = actual
            }

            return true
        } catch (e: Exception) {
            daemonLogger.log(HinaLogLevel.Warn, "转换旧版本用户数据失败!", e, prefix = "兼容性")
        }

        return false
    }

    private fun handleDuplication(users: List<CometUser>, current: CometUser): CometUser {
        val duplicatedUser = users.parallelStream().filter { it.id == current.id }.collect(Collectors.toList())

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