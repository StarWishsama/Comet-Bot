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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.managers.PermissionManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.permission.CometPermission
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.service.compatibility.data.OldCometUser
import io.github.starwishsama.comet.service.compatibility.data.OldGroupConfig
import io.github.starwishsama.comet.utils.copyAndRename
import io.github.starwishsama.comet.utils.parseAsClass
import io.github.starwishsama.comet.utils.serialize.isUsable
import java.io.File
import java.util.*
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
    fun upgradeUserData(userData: File): Boolean {
        val userTree = mapper.readTree(userData)

        // Update point to coin
        if (userTree.all { !it.isNull && it["checkInPoint"].isDouble }) {
            userTree.fields().forEach { (_, value) ->
                if (value.get("coin") == null) {
                    (value as ObjectNode).put("coin", value["checkInPoint"].asDouble())
                }
            }

            mapper.writeValue(userData, userTree)
            daemonLogger.log(HinaLogLevel.Info, "已更新用户数据积分 -> 硬币", prefix = "兼容性")
        }

        if (userTree.all { !it.isNull && !it["uuid"].isNull }) {
            if (userTree.any { !it.isNull && !it["uuid"].isTextual }) {
                userTree.fields().forEach { (_, value) ->
                    if (!value["uuid"].isTextual) {
                        (value as ObjectNode).put("uuid", UUID.randomUUID().toString())
                    }
                }

                mapper.writeValue(userData, userTree)
                daemonLogger.log(HinaLogLevel.Info, "已修复用户数据 UUID", prefix = "兼容性")
            }

            // Fix user id missing, GitHub#355
            if (userTree.any { !it.isNull && it["id"].isNull || it["id"].asLong() == 0L }) {
                userTree.fields().forEach { (key, value) ->
                    if (value["id"].asLong(-1) == 0L) {
                        (value as ObjectNode).put("id", key.toLong())
                    }
                }

                mapper.writeValue(userData, userTree)
                daemonLogger.log(HinaLogLevel.Info, "已修复用户数据 ID", prefix = "兼容性")
            }

            return true
        }

        val oldUser: MutableMap<Long, OldCometUser>

        try {
            daemonLogger.log(HinaLogLevel.Info, "已检测到旧版本配置 ${userData.name}, 正在迁移...", prefix = "兼容性")

            oldUser = userData.parseAsClass()
        } catch (e: Exception) {
            daemonLogger.log(HinaLogLevel.Warn, "转换旧版本配置 ${userData.name} 数据失败!", e, prefix = "兼容性")
            return false
        }

        val newUser = mutableMapOf<Long, CometUser>()

        oldUser.forEach { (id, old) ->
            val permissions = mutableSetOf<CometPermission>()

            if (old.permissions.isNotEmpty()) {
                old.permissions.forEach permission@{
                    permissions.add(PermissionManager.getPermission(it) ?: return@permission)
                }
            }

            newUser[id] = CometUser(
                old.id,
                UUID.randomUUID(),
                old.lastCheckInTime,
                old.checkInPoint,
                old.checkInTime,
                old.r6sAccount,
                old.level,
                old.lastExecuteTime,
                permissions
            )
        }

        CometVariables.cometUsers.putAll(newUser)
        return false
    }

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
            if (du.coin > current.coin || current.coin == 0.0) {
                return du
            }
        }

        return current
    }
}