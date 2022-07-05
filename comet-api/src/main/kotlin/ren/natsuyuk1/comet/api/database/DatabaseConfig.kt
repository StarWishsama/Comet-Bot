package ren.natsuyuk1.comet.api.database

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import java.io.File
import java.sql.Connection.*

enum class DatabaseType {
    SQLITE {
        override val defaultConnectionString: String by lazy {
            "jdbc:sqlite:${resolveDirectory("sqlite.db").absPath}"
        }

        override val defaultMaxPoolSize: Int = 1
        override val driverPath: String = "org.sqlite.JDBC"
        override val defaultIsolationLevel: String = "SERIALIZABLE"
    },
    POSTGRESQL {
        override val defaultConnectionString: String = "jdbc:postgresql://localhost:5432/comet"
        override val defaultMaxPoolSize: Int = 10
        override val driverPath: String = "org.postgresql.Driver"
        override val defaultIsolationLevel: String = "REPEATABLE_READ"
    };

    abstract val defaultConnectionString: String
    abstract val defaultMaxPoolSize: Int
    abstract val driverPath: String
    abstract val defaultIsolationLevel: String
}

object DatabaseConfig : PersistDataFile<DatabaseConfig.Data>(
    File(configDirectory, "database.yml"),
    Data(),
    Yaml()
) {
    @Serializable
    data class Data(
        @Comment("可选的类型有: SQLITE, POSTGRESQL")
        val type: DatabaseType = DatabaseType.SQLITE,
        @Comment("jdbc 风格的数据库连接地址, 如 jdbc:postgresql://localhost:5432/comet")
        val connectionString: String = type.defaultConnectionString,
        @Comment("连接数据库所使用的用户名, SQLite 不必填写")
        val user: String = "",
        @Comment("连接数据库所使用的密码, SQLite 不必填写")
        val password: String = "",
        @Comment("数据库连接池最大大小, 通常不需要改动")
        val maxPoolSize: Int = type.defaultMaxPoolSize,
        @Comment("数据库隔离等级, 通常不需要改动")
        @SerialName("isolationLevel")
        internal val _isolationLevel: String = type.defaultIsolationLevel,
    ) {
        val isolationLevel: Int
            get() = when (_isolationLevel) {
                "NONE" -> TRANSACTION_NONE
                "READ_UNCOMMITTED" -> TRANSACTION_READ_UNCOMMITTED
                "READ_COMMITTED" -> TRANSACTION_READ_COMMITTED
                "REPEATABLE_READ" -> TRANSACTION_REPEATABLE_READ
                "SERIALIZABLE" -> TRANSACTION_SERIALIZABLE
                else -> error("No such field '$_isolationLevel', '${type.defaultIsolationLevel}' is default value for $type")
            }
    }
}
