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

import io.github.starwishsama.comet.api.thirdparty.penguinstats.PenguinStats
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.message.data.MessageChain

object PenguinStatService {
    fun queryItem(args: List<String>): MessageChain {
        if (args.size == 1) {
            return "/pgs item [物品名]".toChain()
        }

        return PenguinStats.getItemDropInfo(args[1]).toChain()
    }

    fun forceUpdate(user: CometUser): MessageChain {
        return if (user.compareLevel(UserLevel.ADMIN)) {
            PenguinStats.forceUpdate()
            "正在更新企鹅物流数据...".toChain()
        } else {
            "你没有权限!".toChain()
        }
    }
}