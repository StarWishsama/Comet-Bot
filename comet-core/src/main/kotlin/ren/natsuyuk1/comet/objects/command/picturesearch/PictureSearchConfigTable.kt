package ren.natsuyuk1.comet.objects.command.picturesearch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.PictureSearchSource
import java.util.*

object PictureSearchConfigTable : Table("picture_search_config") {
    private val id = reference("user_id", UserTable)
    private val searchPlatform = enumeration<PictureSearchSource>("search_source").default(PictureSearchSource.SAUCENAO)

    override val primaryKey = PrimaryKey(id)

    suspend fun setPlatform(
        uuid: UUID,
        searchSource: PictureSearchSource
    ): Unit = withContext(Dispatchers.IO) {
        if (select { id eq uuid }.empty()) {
            insert {
                it[id] = uuid
                it[searchPlatform] = searchSource
            }
        } else {
            update({ id eq uuid }) {
                it[searchPlatform] = searchSource
            }
        }
    }

    suspend fun getPlatform(uuid: UUID): PictureSearchSource? = withContext(Dispatchers.IO) {
        select {
            id eq uuid
        }.map {
            it[searchPlatform]
        }.firstOrNull()
    }
}
