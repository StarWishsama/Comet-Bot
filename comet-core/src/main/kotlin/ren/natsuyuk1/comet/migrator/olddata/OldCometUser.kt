package ren.natsuyuk1.comet.migrator.olddata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.utils.time.yyMMddWithTimePattern
import java.time.LocalDateTime
import java.util.*

@Serializable
data class OldCometUser(
    val id: Long,
    @Serializable(UUIDSerializer::class)
    val uuid: UUID,
    @Serializable(LocalDateTimeSerializer::class)
    var checkInDateTime: LocalDateTime = LocalDateTime.now().minusDays(1),
    var coin: Double = 0.0,
    var checkInCount: Int = 0,
    var r6sAccount: String = "",
    var level: OldUserLevel = OldUserLevel.USER,
    var triggerCommandTime: Long = -1,
    var genshinGachaPool: Int = 0,
)

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("uuid", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("uuid", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), yyMMddWithTimePattern)
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(yyMMddWithTimePattern.format(value))
    }
}

@Serializable
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

@Serializable
data class OldCometPermission(
    val name: String,
    val defaultLevel: OldUserLevel
)
