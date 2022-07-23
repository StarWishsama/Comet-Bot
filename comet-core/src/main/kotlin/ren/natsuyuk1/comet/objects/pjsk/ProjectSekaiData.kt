/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.objects.pjsk

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getCurrentEventInfo
import java.util.*

object ProjectSekaiDataTable : IdTable<Int>("pjsk_data") {
    override val id: Column<EntityID<Int>> = integer("id").entityId()
    val currentEventID = integer("current_event_id")
    val startTime: Column<Long> = long("start_time")
    val endTime: Column<Long> = long("end_time")
    val name = text("name")
}

class ProjectSekaiData(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, ProjectSekaiData>(ProjectSekaiDataTable) {
        suspend fun initData() {
            val currentEvent = cometClient.getCurrentEventInfo()

            transaction {
                new(0) {
                    currentEventID = currentEvent.eventId
                    startTime = currentEvent.eventInfo.startAt
                    endTime = currentEvent.eventInfo.closedAt
                    name = currentEvent.eventInfo.name
                }
            }
        }

        suspend fun updateData() {
            val timestamp = Clock.System.now().epochSeconds
            val currentEvent = cometClient.getCurrentEventInfo()

            transaction {
                val data = ProjectSekaiData.all()

                if (data.empty()) {
                    runBlocking { initData() }
                    return@transaction
                } else {
                    val info = data.first()

                    if (info.endTime < timestamp) {
                        info.apply {
                            currentEventID = currentEvent.eventId
                            startTime = currentEvent.eventInfo.startAt
                            endTime = currentEvent.eventInfo.closedAt
                            name = currentEvent.eventInfo.name
                        }
                    }
                }
            }
        }

        fun getCurrentEventInfo(): ProjectSekaiData? = transaction { ProjectSekaiData.all().firstOrNull() }
    }

    var currentEventID by ProjectSekaiDataTable.currentEventID
    var startTime by ProjectSekaiDataTable.startTime
    var endTime by ProjectSekaiDataTable.endTime
    var name by ProjectSekaiDataTable.name
}


/**
 * 储存与 Project Sekai: Colorful Stage 有关的数据
 *
 * id -> 绑定用户唯一 ID
 * user_id -> roject Sekai 游戏用户唯一 ID
 *
 */
object ProjectSekaiUserDataTable : IdTable<UUID>("pjsk_user_data") {
    override val id: Column<EntityID<UUID>> = reference("user", UserTable.id).index()
    override val primaryKey = PrimaryKey(id)

    val userID = long("user_id")
    val lastQueryScore = long("last_query_score").default(0L)
    val lastQueryPosition = integer("last_query_position").default(0)
}

class ProjectSekaiUserData(id: EntityID<UUID>) : Entity<UUID>(id) {
    // 代表玩家的 Project Sekai 唯一 ID
    var userID by ProjectSekaiUserDataTable.userID

    // 上次查询时的活动 pt
    var lastQueryScore by ProjectSekaiUserDataTable.lastQueryScore

    // 上次查询时的活动排名
    var lastQueryPosition by ProjectSekaiUserDataTable.lastQueryPosition

    companion object : EntityClass<UUID, ProjectSekaiUserData>(ProjectSekaiUserDataTable) {
        fun isBound(uuid: UUID): Boolean = transaction {
            !find { ProjectSekaiUserDataTable.id eq uuid }.empty()
        }

        fun createData(uuid: UUID, userId: Long) = transaction {
            ProjectSekaiUserDataTable.insert {
                it[id] = uuid
                it[userID] = userId
            }
        }

        fun updateID(uuid: UUID, userID: Long) = transaction {
            (findById(uuid) ?: return@transaction).userID = userID
        }

        fun getUserPJSKData(uuid: UUID) = transaction {
            find { ProjectSekaiUserDataTable.id eq uuid }.firstOrNull()
        }
    }
}
