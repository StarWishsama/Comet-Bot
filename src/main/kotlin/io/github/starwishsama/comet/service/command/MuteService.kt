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
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain

object MuteService {

    suspend fun doRandomMute(event: GroupMessageEvent) {
        val iterator = event.group.members.iterator()
        var index = 0
        var randomIndex = RandomUtil.randomInt(0, event.group.members.size)
        var target: Long = -1
        while (iterator.hasNext()) {
            val member = iterator.next()
            if (index == randomIndex) {
                if (member.isAdministrator()) {
                    randomIndex++
                    continue
                }
                target = member.id
            }
            index++
        }
        doMute(event.group, target, RandomUtil.randomLong(1, 2592000).toInt(), false)
    }

    suspend fun doMute(group: Group, id: Long, muteTime: Int, isAll: Boolean): MessageChain {
        if (group.botAsMember.isOperator()) {
            if (isAll) {
                group.settings.isMuteAll = !group.settings.isMuteAll
                return if (group.settings.isMuteAll) {
                    CometUtil.toChain("The World!")
                } else {
                    CometUtil.toChain("然后时间开始流动")
                }
            }

            if (group.botAsMember.id == id) {
                return CometUtil.toChain("不能踢出机器人")
            }

            for (member in group.members) {
                if (member.id == id) {
                    if (member.isOperator()) {
                        return CometUtil.toChain("不能踢出管理员")
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
                        else -> CometUtil.toChain("禁言时间有误, 可能是格式错误, 范围: (0s, 30days]")
                    }
                }
            }

            return CometUtil.toChain("找不到此用户")
        } else {
            return CometUtil.toChain("机器人需要管理员权限才能进行禁言")
        }
    }

    fun getMuteTime(message: String): Int {
        if (message.isNumeric()) {
            return message.toInt()
        }

        var banTime: Long

        val dayRegex = Regex("""(\d{1,2})[dD天]""")
        val hourRegex = Regex("""(\d{1,2})(h|H|小时|时)""")
        val minRegex = Regex("""(\d{1,2})(分钟|分|m|M)""")
        val secRegex = Regex("""(\d{1,7})(分钟|分|m|M)""")

        banTime = dayRegex.find(message)?.groups?.get(1)?.value?.toLong()?.times(24 * 60 * 60) ?: 0L
        banTime += hourRegex.find(message)?.groups?.get(1)?.value?.toLong()?.times(60 * 60) ?: 0L
        banTime += minRegex.find(message)?.groups?.get(1)?.value?.toLong()?.times(60) ?: 0L
        banTime += secRegex.find(message)?.groups?.get(1)?.value?.toLong() ?: 0L

        return banTime.toInt()
    }
}
