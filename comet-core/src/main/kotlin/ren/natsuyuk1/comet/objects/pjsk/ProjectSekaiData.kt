/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.objects.pjsk

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getEventList
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.PJSKEventPredictionInfo
import java.util.*

private val logger = KotlinLogging.logger {}

object ProjectSekaiDataTable : IdTable<Int>("pjsk_data") {
    override val id: Column<EntityID<Int>> = integer("id").entityId()
    val currentEventID = integer("current_event_id")
    val startTime: Column<Long> = long("start_time")
    val aggregateTime: Column<Long> = long("aggregate_at")
    val endTime: Column<Long> = long("end_time")
    val name = text("name")
    val eventPredictionData = text("event_prediction_data")
    val eventPredictionUpdateTime = timestamp("event_prediction_update_time")
}

class ProjectSekaiData(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, ProjectSekaiData>(ProjectSekaiDataTable) {
        private suspend fun initData() {
            kotlin.runCatching {
                Pair(cometClient.getEventList(1).data.first(), cometClient.getRankPredictionInfo())
            }.onSuccess { (eventInfo, pred) ->
                transaction {
                    new(0) {
                        currentEventID = eventInfo.id
                        startTime = eventInfo.startAt
                        aggregateTime = eventInfo.aggregateAt
                        endTime = eventInfo.closedAt
                        name = eventInfo.name
                        eventPredictionData = json.encodeToString(pred)
                        eventPredictionUpdateTime = Clock.System.now()
                    }
                }
            }.onFailure {
                logger.warn(it) { "获取 Project Sekai 活动信息失败, 可能是上游服务器异常" }
            }
        }

        suspend fun updateEventInfo(force: Boolean = false) {
            var init = false
            newSuspendedTransaction {
                if (ProjectSekaiData.all().empty()) {
                    initData()
                    init = true
                }
            }

            if (init) return

            val timestamp = System.currentTimeMillis()

            kotlin.runCatching {
                cometClient.getEventList(1).data.first()
            }.onSuccess { currentEvent ->
                transaction {
                    val info = ProjectSekaiData.all().first()

                    if (info.endTime < timestamp || force) {
                        info.apply {
                            currentEventID = currentEvent.id
                            startTime = currentEvent.startAt
                            aggregateTime = currentEvent.aggregateAt
                            endTime = currentEvent.closedAt
                            name = currentEvent.name
                        }

                        transaction {
                            val updateCount = ProjectSekaiUserDataTable.update {
                                it[lastQueryPosition] = 0
                                it[lastQueryScore] = 0
                            }

                            /* ktlint-disable max-line-length */
                            logger.debug { "Event has updated, operated $updateCount row(s), user data total ${ProjectSekaiUserDataTable.fields.size}" }
                            /* ktlint-enable max-line-length */
                        }
                    }
                }
            }.onFailure {
                logger.warn(it) { "获取 Project Sekai 活动信息失败, 可能是上游服务器异常" }
            }
        }

        suspend fun updatePredictionData() {
            var init = false
            newSuspendedTransaction {
                if (ProjectSekaiData.all().empty()) {
                    initData()
                    init = true
                }
            }

            if (init) return

            val pjskData = transaction { ProjectSekaiData.all().first() }

            val timestamp = Clock.System.now()

            if ((timestamp - pjskData.eventPredictionUpdateTime).inWholeHours >= 1) {
                kotlin.runCatching {
                    cometClient.getRankPredictionInfo()
                }.onSuccess { pred ->
                    transaction {
                        pjskData.apply {
                            eventPredictionData = json.encodeToString(pred)
                            eventPredictionUpdateTime = timestamp
                        }
                    }
                }.onFailure {
                    logger.warn(it) { "获取 Project Sekai 活动积分预测信息失败, 可能是上游服务器异常" }
                }
            }
        }

        fun getCurrentEventInfo(): ProjectSekaiData? = transaction { ProjectSekaiData.all().firstOrNull() }

        fun getCurrentPredictionInfo(): PJSKEventPredictionInfo? =
            transaction {
                getCurrentEventInfo()?.eventPredictionData?.let {
                    json.decodeFromString(it)
                }
            }

        fun getPredictionInfoTime(): Instant? =
            transaction {
                getCurrentEventInfo()?.eventPredictionUpdateTime
            }
    }

    var currentEventID by ProjectSekaiDataTable.currentEventID
    var startTime by ProjectSekaiDataTable.startTime
    var aggregateTime by ProjectSekaiDataTable.aggregateTime
    var endTime by ProjectSekaiDataTable.endTime
    var name by ProjectSekaiDataTable.name
    var eventPredictionData by ProjectSekaiDataTable.eventPredictionData
    var eventPredictionUpdateTime by ProjectSekaiDataTable.eventPredictionUpdateTime
}

/**
 * 储存与 Project Sekai: Colorful Stage 有关的数据
 *
 * id -> 绑定用户唯一 ID
 * user_id -> roject Sekai 游戏用户唯一 ID
 *
 */
object ProjectSekaiUserDataTable : IdTable<UUID>("pjsk_user_data") {
    override val id: Column<EntityID<UUID>> = uuid("user").entityId()
    override val primaryKey = PrimaryKey(id)

    val userID = long("user_id")
    val lastQueryScore = long("last_query_score").default(0L)
    val lastQueryPosition = integer("last_query_position").default(0)
}

class ProjectSekaiUserData(id: EntityID<UUID>) : UUIDEntity(id) {
    // 代表玩家的 Project Sekai 唯一 ID
    var userID by ProjectSekaiUserDataTable.userID

    // 上次查询时的活动 pt
    var lastQueryScore by ProjectSekaiUserDataTable.lastQueryScore

    // 上次查询时的活动排名
    var lastQueryPosition by ProjectSekaiUserDataTable.lastQueryPosition

    fun updateInfo(score: Long, rank: Int) {
        transaction {
            lastQueryScore = score
            lastQueryPosition = rank
        }
    }

    companion object : UUIDEntityClass<ProjectSekaiUserData>(ProjectSekaiUserDataTable) {
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
