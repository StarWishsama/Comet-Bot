/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.user

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * [UserTable]
 *
 * 用户数据表
 *
 */
object UserTable : IdTable<Long>("id") {
    override val id: Column<EntityID<Long>> = long("id").entityId()

    val checkInDate = timestamp("check_in_date")
    val coin = double("coin")
    val checkInTime = integer("check_in_time")
    val userGroup = varchar("user_group", 30)
    val r6sAccount = varchar("r6s_account", 15)
    val triggerCommandTime = timestamp("trigger_command_time")
    val genshinGachaPool: Column<Int> = integer("genshin_gacha_pool")
}

/**
 * [CometUser]
 *
 * 用户数据
 */
class CometUser(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CometUser>(UserTable)

    var checkInDate by UserTable.checkInDate
    var coin by UserTable.coin
    var checkInTime by UserTable.checkInTime
    var userGroup by UserTable.userGroup
    var r6sAccount by UserTable.r6sAccount
    var triggerCommandTime by UserTable.triggerCommandTime
    var genshinGachaPool by UserTable.genshinGachaPool
}

/**
 * [UserPermissionTable]
 *
 *
 */
object UserPermissionTable : LongIdTable() {
    val user = reference("user", UserTable.id).index()
    val permission = varchar("permission", 255)
}

class UserPermission(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserPermission>(UserPermissionTable)

    val user by UserPermissionTable.user
    val permission by UserPermissionTable.permission
}
