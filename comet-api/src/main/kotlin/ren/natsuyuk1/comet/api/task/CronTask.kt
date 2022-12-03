package ren.natsuyuk1.comet.api.task

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object CronTasks : IdTable<String>("cron_tasks") {
    override val id = varchar("id", 128).entityId()

    val cron: Column<String> = varchar("cron", 80)
    val lastExecution = this.timestamp("last_execution").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

/**
 * A cron task from database
 *
 * @property cron [com.cronutils.model.Cron] object
 * @property lastExecution last execution time of this task
 */
class CronTask(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, CronTask>(CronTasks)

    var cron by CronTasks.cron.transform(
        toColumn = {
            it.asString()
        },
        toReal = {
            parseCron(it)
        }
    )

    var lastExecution by CronTasks.lastExecution

    /**
     * Get next execution time of this task
     * from now or a specified time
     *
     * @param time next execution time from this time
     */
    fun nextExecution(time: Instant = Clock.System.now()): Instant =
        cron.nextExecutionTime(lastExecution ?: time) ?: time
}
