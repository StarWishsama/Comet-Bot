package ren.natsuyuk1.comet.migrator.olddata

import ren.natsuyuk1.comet.api.user.UserLevel
import java.time.LocalDateTime
import java.util.*

data class OldCometUser(
    val id: Long,
    val uuid: UUID,
    var checkInDateTime: LocalDateTime = LocalDateTime.now().minusDays(1),
    var coin: Double = 0.0,
    var checkInCount: Int = 0,
    var r6sAccount: String = "",
    var level: OldUserLevel = OldUserLevel.USER,
    var triggerCommandTime: Long = -1,
    var genshinGachaPool: Int = 0,
    private val permissions: MutableSet<OldCometPermission> = mutableSetOf(),
)

enum class OldUserLevel {
    USER, ADMIN, OWNER, CONSOLE;
}

fun OldUserLevel.toUserLevel(): UserLevel =
    when (this) {
        OldUserLevel.USER -> UserLevel.USER
        OldUserLevel.ADMIN -> UserLevel.ADMIN
        OldUserLevel.OWNER -> UserLevel.OWNER
        OldUserLevel.CONSOLE -> UserLevel.CONSOLE
    }


data class OldCometPermission(
    val name: String,
    val defaultLevel: OldUserLevel
)
