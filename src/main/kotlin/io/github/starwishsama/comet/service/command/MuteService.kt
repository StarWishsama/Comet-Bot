/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.command

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.math.TimeUtil
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain

object MuteService {
    suspend fun doRandomMute(event: GroupMessageEvent) {
        val target: Long = event.group.members.filter { !it.isAdministrator() }.randomOrNull()?.id ?: -1
        event.subject.sendMessage(doMute(event.group, target, RandomUtil.randomLong(1, 2592000).toInt(), false))
    }

    suspend fun doMute(group: Group, id: Long, muteTime: Int, isAll: Boolean): MessageChain {
        if (group.botAsMember.isOperator()) {
            if (isAll) {
                group.settings.isMuteAll = !group.settings.isMuteAll
                return if (group.settings.isMuteAll) {
                    CometUtil.toChain("全体禁言已开启")
                } else {
                    CometUtil.toChain("全体禁言已关闭")
                }
            }

            if (group.botAsMember.id == id) {
                return CometUtil.toChain("不能禁言机器人")
            }

            val member = group.members.find { it.id == id }

            if (member != null) {
                if (member.isOperator()) {
                    return CometUtil.toChain("不能禁言管理员")
                }

                if (member.isMuted) {
                    member.unmute()
                    CometUtil.toChain("解禁 ${member.nameCardOrNick} 成功")
                }

                return when (muteTime) {
                    in 1..2592000 -> {
                        member.mute(muteTime)
                        CometUtil.toChain("禁言 ${member.nameCardOrNick} 成功")
                    }
                    0 -> {
                        member.unmute()
                        CometUtil.toChain("解禁 ${member.nameCardOrNick} 成功")
                    }
                    else -> {
                        CometUtil.toChain("禁言时间有误, 可能是格式错误, 范围: (0s, 30days]")
                    }
                }
            }

            return CometUtil.toChain("找不到此用户")
        } else {
            return CometUtil.toChain("机器人需要管理员权限才能进行禁言")
        }
    }

    fun getMuteTime(message: String): Int {
        // 30*24*60*60, a month
        val maxBanTime = 2592000
        var banTime = TimeUtil.parseTextTime(message)

        if (banTime > maxBanTime) {
            banTime = maxBanTime
        }

        return banTime
    }
}
