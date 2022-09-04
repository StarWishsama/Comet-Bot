package ren.natsuyuk1.comet.pusher

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import ren.natsuyuk1.comet.objects.config.BiliBiliSubscriberTable
import ren.natsuyuk1.comet.utils.sql.MapTable
import ren.natsuyuk1.comet.utils.sql.SQLDatabaseMap

object CometPusherDataTable: IntIdTable("comet_pusher_data") {
    val pusherName = text("pusher_name")
}

class CometPusherData(id: EntityID<Int>): IntEntity(id) {
    val pusherName by CometPusherDataTable.pusherName
    val pushContext = SQLDatabaseMap(id, CometPusherContextTable)

    companion object : IntEntityClass<CometPusherData>(CometPusherDataTable) {
        fun insertPushContext(pusher: CometPusher, context: CometPushContext) {
            find { CometPusherDataTable.pusherName eq pusher.name }.firstOrNull()?.pushContext?.put(context.id, context.toJson())
        }

        fun isDuplicated(pusher: CometPusher, id: String): Boolean? =
            find { CometPusherDataTable.pusherName eq pusher.name }.firstOrNull()?.pushContext?.containsKey(id)
    }
}

object CometPusherContextTable: MapTable<Int, String, String>("comet_pusher_context") {
    override val id: Column<EntityID<Int>> = BiliBiliSubscriberTable.reference("id", CometPusherDataTable)
    override val key: Column<String> = text("push_context_id")
    override val value: Column<String> = text("push_context")
}
