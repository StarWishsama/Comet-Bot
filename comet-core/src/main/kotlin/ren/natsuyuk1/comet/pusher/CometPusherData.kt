package ren.natsuyuk1.comet.pusher

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import kotlin.time.Duration.Companion.days

object CometPusherContextTable : IdTable<String>("comet_pusher_context") {
    override val id: Column<EntityID<String>> = text("push_context_id").entityId()
    val pusherName: Column<String> = text("push_name")
    val context: Column<String> = text("push_context")
    val date = datetime("push_date").default(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))

    override val primaryKey: Table.PrimaryKey = PrimaryKey(id)
}

class CometPusherContext(id: EntityID<String>) : Entity<String>(id) {
    var pusherName by CometPusherContextTable.pusherName
    var context by CometPusherContextTable.context
    val date by CometPusherContextTable.date

    companion object : EntityClass<String, CometPusherContext>(CometPusherContextTable) {
        fun insertPushContext(pusherName: String, context: CometPushContext) {
            new(context.id) {
                this.pusherName = pusherName
                this.context = context.toJson()
            }
        }

        fun isDuplicated(pusherName: String, id: String): Boolean =
            !find { CometPusherContextTable.pusherName eq pusherName and (CometPusherContextTable.id eq id) }.empty()

        fun deleteOutdatedContext(pusherName: String) {
            if (CometPusherContextTable.columns.size <= 50) {
                return
            }

            val queryTime = Clock.System.now().minus(1.days).toLocalDateTime(TimeZone.currentSystemDefault())

            CometPusherContextTable.deleteWhere {
                CometPusherContextTable.pusherName eq pusherName and (CometPusherContextTable.date less queryTime)
            }
        }
    }
}
