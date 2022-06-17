/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.user

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import ren.natsuyuk1.comet.utils.sql.SQLDatabaseSet
import ren.natsuyuk1.comet.utils.sql.SetTable

/**
 * [UserTable]
 *
 * 用户数据表
 *
 */
object UserTable : IdTable<Long>("user_data") {
    override val id: Column<EntityID<Long>> = long("id").entityId()
    override val primaryKey = PrimaryKey(id)

    val checkInDate = timestamp("check_in_date")
    val coin = double("coin")
    val checkInTime = integer("check_in_time")
    val userLevel = enumeration<UserLevel>("user_level")
    val r6sAccount = varchar("r6s_account", 15)
    val triggerCommandTime = timestamp("trigger_command_time")
    val genshinGachaPool: Column<Int> = integer("genshin_gacha_pool")
}

/**
 * [CometUser]
 *
 * 用户数据
 */
class CometUser(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, CometUser>(UserTable)

    var checkInDate by UserTable.checkInDate
    var coin by UserTable.coin
    var checkInTime by UserTable.checkInTime
    val permissions = SQLDatabaseSet(id, UserPermissionTable)
    var userLevel by UserTable.userLevel
    var r6sAccount by UserTable.r6sAccount
    var triggerCommandTime by UserTable.triggerCommandTime
    var genshinGachaPool by UserTable.genshinGachaPool
}

/**
 * [UserPermissionTable]
 *
 *
 */
object UserPermissionTable : SetTable<Long, String>("user_permission") {
    override val id: Column<EntityID<Long>> = reference("user", UserTable.id).index()
    override val value: Column<String> = varchar("permission_node", 255)
}

class UserPermission(id: EntityID<Long>) : LongEntity(id) {
    companion object : EntityClass<Long, Entity<Long>>(UserPermissionTable)

    val user by UserPermissionTable.id
    val permissionNode by UserPermissionTable.value
}
