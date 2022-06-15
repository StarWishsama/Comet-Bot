/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.objects.pjsk

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import ren.natsuyuk1.comet.user.UserTable

/**
 * 储存与 Project Sekai: Colorful Stage 有关的数据
 *
 * id -> Project Sekai 游戏用户唯一 ID
 * user_id -> 绑定用户的唯一 ID
 *
 */
object ProjectSekaiDataTable : IdTable<ULong>("pjsk_data") {
    override val id: Column<EntityID<ULong>> = UserTable.ulong("id").entityId()
    override val primaryKey = PrimaryKey(id)

    val userID = ulong("user_id")
}

class ProjectSekaiData(id: EntityID<ULong>) : Entity<ULong>(id) {
    companion object : EntityClass<ULong, ProjectSekaiData>(ProjectSekaiDataTable)

    var userID by ProjectSekaiDataTable.userID
}
