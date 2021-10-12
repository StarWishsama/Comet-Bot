/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MemberJoinRequestEvent

@NListener("入群申请自动通过")
object GroupRequestListener : INListener {
    @EventHandler
    fun listen(event: MemberJoinRequestEvent) {
        val cfg = GroupConfigManager.getConfig(event.groupId) ?: return

        if (cfg.autoAccept) {
            runBlocking {
                if (cfg.autoAcceptCondition.isEmpty() || cfg.autoAcceptCondition == event.message) {
                    event.accept()
                } else {
                    /**
                     * @TODO 添加自定义拒绝理由
                     */
                    event.reject(false, "")
                }
            }
        }
    }
}
