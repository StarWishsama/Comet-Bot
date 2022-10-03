package ren.natsuyuk1.comet.objects.command.now

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import java.util.*

private typealias NTable = NowCmdConfigTable

object NowCmdConfigTable : Table("now_cmd_config") {
    private val platformType = enumerationByName<LoginPlatform>("type", 20)
    private val isGroup = bool("is_group")
    private val id = long("id")
    private val config = text("config")

    override val primaryKey = PrimaryKey(platformType, isGroup, id)

    suspend fun setConfig(
        platform: LoginPlatform,
        isGroup: Boolean,
        id: Long,
        config: Config
    ): Unit = withContext(Dispatchers.IO) {
        if (!hasConfig(platform, isGroup, id)) {
            insert {
                it[platformType] = platform
                it[NowCmdConfigTable.isGroup] = isGroup
                it[NowCmdConfigTable.id] = id
                it[NowCmdConfigTable.config] = Json.encodeToString(config)
            }
        } else {
            update({ NowCmdConfigTable.id eq id and (NowCmdConfigTable.isGroup eq isGroup) and (platformType eq platform) }) {
                it[platformType] = platform
                it[NowCmdConfigTable.isGroup] = isGroup
                it[NowCmdConfigTable.id] = id
                it[NowCmdConfigTable.config] = Json.encodeToString(config)
            }
        }
    }

    suspend fun hasConfig(
        platform: LoginPlatform,
        isGroup: Boolean,
        id: Long
    ): Boolean = withContext(Dispatchers.IO) {
        select {
            (platformType eq platform) and
                (NowCmdConfigTable.isGroup eq isGroup) and
                (NowCmdConfigTable.id eq id)
        }.firstOrNull() != null
    }

    suspend fun getConfig(
        platform: LoginPlatform,
        isGroup: Boolean,
        id: Long
    ): Config? = withContext(Dispatchers.IO) {
        select {
            (platformType eq platform) and
                (NowCmdConfigTable.isGroup eq isGroup) and
                (NowCmdConfigTable.id eq id)
        }.map {
            Json.decodeFromString<Config>(it[config])
        }.firstOrNull()
    }
}

@Serializable
class Config(
    val formatter: String? = null,

    val timezones: List<Pair<@Serializable(TimeZoneSerializer::class) TimeZone, String?>>? = null
)

object TimeZoneSerializer : KSerializer<TimeZone> {
    override fun deserialize(decoder: Decoder): TimeZone {
        return TimeZone.getTimeZone(decoder.decodeString())
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TimeZone", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TimeZone) {
        encoder.encodeString(value.id)
    }
}
