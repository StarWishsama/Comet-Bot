package ren.natsuyuk1.comet.api.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = mu.KotlinLogging.logger {}

object DatabaseManager {
    val database: Database
        get() = databaseInstance ?: error("Database instance is not prepared")

    private var databaseInstance: Database? = null

    /**
     * load database, should be invoked before any table operations
     *
     * use config [DatabaseConfig]
     */
    fun loadDatabase(): Database {
        val dbConfig = DatabaseConfig.data
        val config = HikariConfig().apply {
            jdbcUrl = dbConfig.connectionString
            driverClassName = dbConfig.type.driverPath
            username = dbConfig.user
            password = dbConfig.password
            maximumPoolSize = dbConfig.maxPoolSize
            if (dbConfig.type == DatabaseType.SQLITE && dbConfig.maxPoolSize > 1) {
                logger.warn { "你正在使用 SQLite, 但 maxPoolSize 被设置为了大于 1 的数," }
                logger.warn { "这将会导致 SQLite 文件出现死锁, 影响使用." }
            }

            val processors = Runtime.getRuntime().availableProcessors()
            if (dbConfig.maxPoolSize >= processors + 2) {
                logger.warn { "检测到的处理器数目为 $processors 但 maxPoolSize 却为 ${dbConfig.maxPoolSize}" }
                logger.warn { "不推荐无脑加连接池大小!" }
                logger.warn { "了解更多: https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing" }
            }
        }
        val dataSource = HikariDataSource(config)
        val db = Database.connect(dataSource)
        TransactionManager.manager.defaultIsolationLevel = dbConfig.isolationLevel
        databaseInstance = db
        return db
    }

    /**
     * load tables and add missing schema for compatible
     *
     * should be invoked before table use
     */
    fun <T : Table> loadTables(vararg table: T) = transaction(database) {
        SchemaUtils.createMissingTablesAndColumns(*table)
    }

    fun loadTables(table: List<Table>) = transaction(database) {
        SchemaUtils.createMissingTablesAndColumns(*(table.toTypedArray()))
    }
}
