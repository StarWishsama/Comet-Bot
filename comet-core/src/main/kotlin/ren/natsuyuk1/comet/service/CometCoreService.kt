package ren.natsuyuk1.comet.service

import kotlinx.coroutines.launch
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.group.GroupSettingManager
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometPersistDataFile
import ren.natsuyuk1.comet.migrator.GitHubRepoMigrator
import ren.natsuyuk1.comet.migrator.UserDataMigrator
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaClient
import ren.natsuyuk1.comet.network.thirdparty.twitter.initSetsuna
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

object CometCoreService {
    var scope = ModuleScope("comet-core-service")

    fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("comet-core-service", parentContext)

        scope.launch {
            initCoreService()
        }
    }

    private suspend fun initCoreService() {
        ProjectSekaiManager.init(scope.coroutineContext)
        scope.launch {
            ArcaeaClient.fetchConstants()
        }
        GroupSettingManager.init(scope.coroutineContext)
        TaskManager.registerTask(1.hours) { HitokotoManager.fetch() }
        startAutoSaveService()
        initSetsuna(scope.coroutineContext)
        try {
            UserDataMigrator.migrate()
            GitHubRepoMigrator.migrate()
        } catch (e: Exception) {
            logger.warn(e) { "在迁移用户数据时出现异常" }
        }
    }

    private fun startAutoSaveService() {
        TaskManager.registerTaskDelayed(CometGlobalConfig.data.dataSaveDuration.minutes) {
            GroupSettingManager.save()

            cometPersistDataFile.forEach {
                it.save()
            }
        }
    }
}
