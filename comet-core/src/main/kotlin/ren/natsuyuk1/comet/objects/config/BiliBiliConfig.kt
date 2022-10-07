package ren.natsuyuk1.comet.objects.config

import moe.sdl.yabapi.data.search.results.UserResult
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.network.thirdparty.bilibili.SearchApi
import ren.natsuyuk1.comet.network.thirdparty.bilibili.UserApi
import ren.natsuyuk1.comet.objects.pusher.SubscribeStatus
import ren.natsuyuk1.comet.utils.sql.SQLDatabaseSet
import ren.natsuyuk1.comet.utils.sql.SetTable

object BiliBiliDataTable : IdTable<Long>() {
    override val id = long("uid").entityId()
    val username = varchar("username", 30)
}

class BiliBiliData(id: EntityID<Long>) : Entity<Long>(id) {
    val username by BiliBiliDataTable.username
    val subscribers = SQLDatabaseSet(id, BiliBiliSubscriberTable)

    companion object : EntityClass<Long, BiliBiliData>(BiliBiliDataTable) {
        suspend fun subscribe(uid: Long, target: Long): SubscribeStatus =
            newSuspendedTransaction {
                if (UserApi.getUserCard(uid) == null) {
                    return@newSuspendedTransaction SubscribeStatus.NOT_FOUND
                }

                val data = findById(uid)

                if (data?.subscribers?.contains(target) == true) {
                    return@newSuspendedTransaction SubscribeStatus.SUBSCRIBED
                }

                data?.subscribers?.add(target) ?: new(uid) {
                    subscribers.add(target)
                }

                SubscribeStatus.SUCCESS
            }

        suspend fun subscribe(name: String, target: Long): SubscribeStatus =
            newSuspendedTransaction {
                val data = find {
                    BiliBiliDataTable.username eq name
                }

                if (data.empty()) {
                    val user = SearchApi.searchUser(name)?.data?.firstOrNull()
                        ?: return@newSuspendedTransaction SubscribeStatus.NOT_FOUND

                    if (user is UserResult) {
                        new(user.mid!!) {
                            subscribers.add(target)
                        }
                        return@newSuspendedTransaction SubscribeStatus.SUCCESS
                    } else {
                        return@newSuspendedTransaction SubscribeStatus.NOT_FOUND
                    }
                }

                data.firstOrNull()?.subscribers?.add(target)
                SubscribeStatus.SUCCESS
            }

        fun unsubscribe(uid: Long, target: Long): SubscribeStatus =
            transaction {
                val data = findById(uid) ?: return@transaction SubscribeStatus.NOT_FOUND

                data.subscribers.remove(target)
                SubscribeStatus.SUCCESS
            }

        suspend fun unsubscribe(username: String, target: Long): SubscribeStatus =
            newSuspendedTransaction {
                var data = find { BiliBiliDataTable.username eq username }.firstOrNull()

                if (data == null) {
                    val user = SearchApi.searchUser(username)?.data?.firstOrNull()
                        ?: return@newSuspendedTransaction SubscribeStatus.NOT_FOUND

                    if (user !is UserResult) {
                        return@newSuspendedTransaction SubscribeStatus.NOT_FOUND
                    }

                    data = findById(user.mid!!) ?: return@newSuspendedTransaction SubscribeStatus.NOT_FOUND
                }

                data.subscribers.remove(target)
                SubscribeStatus.SUCCESS
            }
    }
}

object BiliBiliSubscriberTable : SetTable<Long, Long>("bilibili_subscribers") {
    override val id: Column<EntityID<Long>> = reference("uid", BiliBiliDataTable)
    override val value: Column<Long> = long("push_target")
}
