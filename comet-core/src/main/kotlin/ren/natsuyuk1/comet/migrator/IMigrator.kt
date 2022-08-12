package ren.natsuyuk1.comet.migrator

interface IMigrator {
    suspend fun migrate()
}
