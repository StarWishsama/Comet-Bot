package ren.natsuyuk1.comet.migrator

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.migrator.olddata.OldCometUser
import ren.natsuyuk1.comet.migrator.olddata.toUserLevel
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import java.io.File

private val logger = KotlinLogging.logger {}

object UserDataMigrator : IMigrator {
    private val userCache = mutableMapOf<Long, OldCometUser>()

    override suspend fun migrate() {
        if (!oldFilePath.exists() || !oldFilePath.isDirectory) {
            return
        }

        val userFile = File(oldFilePath, "users.json")

        if (!userFile.exists()) {
            return
        }

        try {
            logger.info { "正在导入用户数据." }

            userCache.putAll(json.decodeFromString(userFile.readTextBuffered()))
            val pendingRemove = mutableListOf<Long>()

            userCache.forEach { (id, user) ->
                transaction {
                    if (CometUser.getUser(id, LoginPlatform.MIRAI) == null) {
                        UserTable.insert {
                            it[platformID] = id
                            it[platform] = LoginPlatform.MIRAI
                            it[coin] = user.coin
                            it[checkInTime] = user.checkInCount

                            val oldTime = user.checkInDateTime

                            it[checkInDate] = LocalDateTime(
                                oldTime.year,
                                oldTime.month,
                                oldTime.dayOfMonth,
                                oldTime.hour,
                                oldTime.minute,
                                oldTime.second,
                                oldTime.nano
                            ).toInstant(TimeZone.currentSystemDefault())
                            it[userLevel] = user.level.toUserLevel()
                        }
                    } else {
                        pendingRemove.add(id)
                    }
                }
            }

            pendingRemove.forEach(userCache::remove)

            logger.info { "已迁移 ${userCache.size} 条用户数据!" }
            userCache.clear()
            userFile.delete()
        } catch (e: SerializationException) {
            logger.warn(e) { "迁移数据时出现了问题" }
        }
    }
}
