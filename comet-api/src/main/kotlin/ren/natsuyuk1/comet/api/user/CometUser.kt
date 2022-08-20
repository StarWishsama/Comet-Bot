/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.user

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mu.KotlinLogging
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.utils.sql.SQLDatabaseSet
import ren.natsuyuk1.comet.utils.sql.SetTable
import java.util.*
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger {}

/**
 * [UserTable]
 *
 * 用户数据表
 *
 */
object UserTable : UUIDTable("comet_user_data") {

    val platformID = long("platform_id")
    val platform = enumeration<LoginPlatform>("platform")
    val checkInDate =
        datetime("check_in_date").default(
            Clock.System.now().minus(1.days).toLocalDateTime(TimeZone.currentSystemDefault())
        )
    val coin = double("coin").default(0.0)
    val level = integer("level").default(0)
    val exp = long("exp").default(0L)
    val checkInTime = integer("check_in_time").default(0)
    val userLevel = enumeration<UserLevel>("user_level").default(UserLevel.USER)
    val triggerCommandTime = timestamp("trigger_command_time").default(Clock.System.now())
}

/**
 * [CometUser]
 *
 * 用户数据
 */
class CometUser(id: EntityID<UUID>) : Entity<UUID>(id) {

    var platformID by UserTable.platformID
    var platform by UserTable.platform
    var checkInDate by UserTable.checkInDate
    var coin by UserTable.coin
    var level by UserTable.level
    var exp by UserTable.exp
    var checkInTime by UserTable.checkInTime
    val permissions = SQLDatabaseSet(id, UserPermissionTable)
    var userLevel by UserTable.userLevel
    var triggerCommandTime by UserTable.triggerCommandTime

    companion object : EntityClass<UUID, CometUser>(UserTable) {
        /**
         * 获取一个不可操作的用户
         */
        val dummyUser = CometUser(
            EntityID(
                UUID.randomUUID(),
                object : IdTable<UUID>("fake_table") {
                    override val id: Column<EntityID<UUID>> = uuid("fake_uuid").entityId()
                }
            )
        )

        fun getUser(id: Long, platform: LoginPlatform) = transaction {
            find {
                UserTable.platform eq platform and (UserTable.platformID eq id)
            }.firstOrNull()
        }

        /**
         * 获取一个 [CometUser] 实例，在不存在时手动创建
         *
         * @return 获取或创建的 [CometUser]
         */
        fun getUserOrCreate(id: Long, platform: LoginPlatform): CometUser = transaction {
            return@transaction getUser(id, platform) ?: create(id, platform)
        }

        /**
         * Create a user to database
         *
         * When register a new user, you **must** provide id and platform.
         *
         * @param id Platform ID
         * @param platform Platform
         *
         * @return user instance
         */
        fun create(id: Long, platform: LoginPlatform): CometUser {
            logger.info { "Creating comet user for $id in ${platform.name}" }
            return transaction {
                new {
                    platformID = id
                    this.platform = platform
                }
            }
        }
    }
}

/**
 * [UserPermissionTable]
 *
 * 用户所拥有的权限列表
 */
object UserPermissionTable : SetTable<UUID, String>("comet_user_permission") {
    override val id: Column<EntityID<UUID>> = reference("user", UserTable.id).index()
    override val value: Column<String> = varchar("permission_node", 255)
}
