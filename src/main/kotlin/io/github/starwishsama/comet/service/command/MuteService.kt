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

    /**
     * 这段代码看起来很神必
     * 但是 It just works.
     * FIXME: 更换为正则表达式更优雅的处理
     */
    fun getMuteTime(message: String): Int {
        if (message.isNumeric()) {
            return message.toInt()
        }

        var banTime = 0L
        var tempTime: String = message
        if (tempTime.indexOf('d') != -1) {
            banTime += (tempTime.substring(0, tempTime.indexOf('d')).toInt() * 24
                    * 60 * 60)
            tempTime = tempTime.substring(tempTime.indexOf('d') + 1)
        } else if (tempTime.contains("天")) {
            banTime += (tempTime.substring(0, tempTime.indexOf('天')).toInt() * 24
                    * 60 * 60)
            tempTime = tempTime.substring(tempTime.indexOf('天') + 1)
        }
        if (tempTime.indexOf('h') != -1) {
            banTime += (tempTime.substring(0, tempTime.indexOf('h')).toInt() * 60
                    * 60)
            tempTime = tempTime.substring(tempTime.indexOf('h') + 1)
        } else if (tempTime.contains("小时")) {
            banTime += (tempTime.substring(0, tempTime.indexOf("时")).toInt() * 60
                    * 60)
            tempTime = tempTime.substring(tempTime.indexOf("时") + 1)
        }
        if (tempTime.indexOf('m') != -1) {
            banTime += tempTime.substring(0, tempTime.indexOf('m')).toInt() * 60
        } else if (tempTime.contains("分钟")) {
            banTime += tempTime.substring(0, tempTime.indexOf("分钟")).toInt() * 60
        }
        return banTime.toInt()
    }
}