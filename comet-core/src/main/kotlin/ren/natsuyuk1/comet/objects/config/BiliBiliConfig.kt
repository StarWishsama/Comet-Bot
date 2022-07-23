package ren.natsuyuk1.comet.objects.config

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.utils.sql.SQLDatabaseSet
import ren.natsuyuk1.comet.utils.sql.SetTable

object BiliBiliDataTable : IdTable<Int>() {
    override val id = integer("uid").entityId()
    val username = varchar("username", 30)
}

class BiliBiliData(id: EntityID<Int>) : Entity<Int>(id) {
    val username by BiliBiliDataTable.username
    val subscribers = SQLDatabaseSet(id, BiliBiliSubscriberTable)

    companion object : EntityClass<Int, BiliBiliData>(BiliBiliDataTable) {
        fun subscribe(uid: Int, target: Long) {
            transaction {
                val data = findById(uid)

                data?.subscribers?.add(target) ?: new(uid) {
                    subscribers.add(target)
                }
            }
        }

        fun unsubscribe(uid: Int, target: Long) {
            transaction {
                val data = findById(uid)

                data?.subscribers?.remove(target) ?: new(uid) {
                    subscribers.remove(target)
                }
            }
        }
    }
}

object BiliBiliSubscriberTable : SetTable<Int, Long>("bilibili_subscribers") {
    override val id: Column<EntityID<Int>> = reference("uid", BiliBiliDataTable)
    override val value: Column<Long> = long("push_target")
}
