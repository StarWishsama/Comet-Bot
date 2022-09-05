package ren.natsuyuk1.comet.pusher

import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.utils.sql.MapTable
import ren.natsuyuk1.comet.utils.sql.SQLDatabaseMap

object CometPusherDataTable: IntIdTable("comet_pusher_data") {
    val pusherName = text("pusher_name")
}

class CometPusherData(id: EntityID<Int>): IntEntity(id) {
    var pusherName by CometPusherDataTable.pusherName
    val pushContext = SQLDatabaseMap(id, CometPusherContextTable)

    companion object : IntEntityClass<CometPusherData>(CometPusherDataTable) {
        fun init(pusherName: String) {
            if (find { CometPusherDataTable.pusherName eq pusherName }.empty()) {
                new {
                    this.pusherName = pusherName
                }
            }
        }

        fun insertPushContext(pusherName: String, context: CometPushContext) {
            find { CometPusherDataTable.pusherName eq pusherName }.firstOrNull()?.pushContext?.put(context.id, context.toJson())
        }

        fun isDuplicated(pusherName: String, id: String): Boolean? =
            find { CometPusherDataTable.pusherName eq pusherName }.firstOrNull()?.pushContext?.containsKey(id)

        fun deleteOutdatedContext(pusherName: String) {
            val queryTime = Clock.System.now()
            val pendingRemoveEntry = mutableSetOf<String>()

            find {
                CometPusherDataTable.pusherName eq pusherName
            }.firstOrNull()?.pushContext?.apply {
                forEach { (k, v) ->
                    val context: MinCometPushContext = json.decodeFromString(v)
                    if ((queryTime - context.createTime).inWholeDays > 1) {
                        pendingRemoveEntry.add(k)
                    }
                }

                pendingRemoveEntry.forEach {
                    this.remove(it)
                }
            }
        }
    }
}

object CometPusherContextTable: MapTable<Int, String, String>("comet_pusher_context") {
    override val id: Column<EntityID<Int>> = reference("id", CometPusherDataTable)
    override val key: Column<String> = text("push_context_id")
    override val value: Column<String> = text("push_context")
}
