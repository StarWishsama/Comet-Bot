package ren.natsuyuk1.comet.test

import kotlinx.coroutines.runBlocking
import ren.natsuyuk1.comet.api.database.DatabaseConfig
import ren.natsuyuk1.comet.api.database.DatabaseManager

fun initTestDatabase() = runBlocking {
    DatabaseConfig.init()
    DatabaseManager.loadDatabase()
}
