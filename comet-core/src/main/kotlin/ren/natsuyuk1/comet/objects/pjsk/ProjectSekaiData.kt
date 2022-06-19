/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.objects.pjsk

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import ren.natsuyuk1.comet.api.user.UserTable

object ProjectSekaiDataTable : IdTable<Int>("pjsk_data") {
    override val id: Column<EntityID<Int>> = integer("current_event_id").entityId()
    val startTime: Column<Long> = long("start_time")
    val endTime: Column<Long> = long("end_time")
}

class ProjectSekaiData(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, ProjectSekaiData>(ProjectSekaiDataTable)

    val currentEventID by ProjectSekaiDataTable.id
    val startTime by ProjectSekaiDataTable.startTime
    val endTime by ProjectSekaiDataTable.endTime
}


/**
 * 储存与 Project Sekai: Colorful Stage 有关的数据
 *
 * id -> Project Sekai 游戏用户唯一 ID
 * user_id -> 绑定用户的唯一 ID
 *
 */
object ProjectSekaiUserDataTable : IdTable<Long>("pjsk_user_data") {
    override val id: Column<EntityID<Long>> = UserTable.long("id").entityId()
    override val primaryKey = PrimaryKey(id)

    val userID = ulong("user_id")
}

class ProjectSekaiUserData(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, ProjectSekaiUserData>(ProjectSekaiUserDataTable)

    // 代表玩家的 Project Sekai 唯一 ID
    var userID by ProjectSekaiUserDataTable.userID
}
