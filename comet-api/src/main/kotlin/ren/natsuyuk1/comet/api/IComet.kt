package ren.natsuyuk1.comet.api

import ren.natsuyuk1.comet.api.user.Group
import java.util.concurrent.ConcurrentLinkedDeque

val cometInstances = ConcurrentLinkedDeque<Comet>()

/**
 * [IComet]
 *
 * 一个 [Comet] 扩展方法，包括常用的获取用户等。
 */
interface IComet {
    /**
     * 获取一个群聊
     *
     * @param id 群聊 ID
     *
     * @return [Group]，可能为空
     */
    suspend fun getGroup(id: Long): Group?
}
