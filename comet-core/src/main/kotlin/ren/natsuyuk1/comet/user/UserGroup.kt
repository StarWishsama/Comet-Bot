/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.user

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import ren.natsuyuk1.comet.utils.sql.SetTable

/**
 * [UserGroup] 代表 [CometUser] 的权限组
 */
@Serializable
data class UserGroup(
    val name: String,
    val alias: List<String>,
    val ownPermission: List<UserPermission>
)

object UserGroupTable : Table("user_group") {
    val name: Column<String> = text("name")

    override val primaryKey = PrimaryKey(name)
}

object UserGroupAliasTable : SetTable<String, String>("user_group_own_permission") {
    override val id = reference("name", UserGroupTable.name).entityId()

    override val value: Column<String> = varchar("alias", 255)

    override val primaryKey = PrimaryKey(id)
}

object UserGroupOwnPermissionTable : SetTable<String, String>("user_group_own_permission") {
    override val id = reference("name", UserGroupTable.name).entityId()

    override val value: Column<String> = varchar("permission", 255)

    override val primaryKey = PrimaryKey(id)
}
