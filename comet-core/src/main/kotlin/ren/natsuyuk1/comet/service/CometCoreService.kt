package ren.natsuyuk1.comet.service

import kotlinx.coroutines.launch
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometPersistDataFile
import ren.natsuyuk1.comet.migrator.GitHubRepoMigrator
import ren.natsuyuk1.comet.migrator.UserDataMigrator
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaHelper
import ren.natsuyuk1.comet.network.thirdparty.bilibili.initYabapi
import ren.natsuyuk1.comet.network.thirdparty.twitter.initSetsuna
import ren.natsuyuk1.comet.objects.config.FeatureConfig
import ren.natsuyuk1.comet.objects.group.GroupSettingManager
import ren.natsuyuk1.comet.pusher.DEFAULT_PUSHERS
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import java.security.GeneralSecurityException
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
        try {
            UserDataMigrator.migrate()
            GitHubRepoMigrator.migrate()
        } catch (e: Exception) {
            logger.warn(e) { "在迁移用户数据时出现异常" }
        }

        startAutoSaveService()
        GroupSettingManager.init(scope.coroutineContext)

        /* init external API */
        initSetsuna(scope.coroutineContext)
        initYabapi()

        scope.launch {
            try {
                if (FeatureConfig.data.arcaea) {
                    ArcaeaHelper.load()
                }
            } catch (e: GeneralSecurityException) {
                logger.warn(e) { "无法加载 Arcaea 数据: 目标主机证书有误" }
            }

            if (FeatureConfig.data.arcaea) {
                ProjectSekaiManager.init(scope.coroutineContext)
            }
        }

        TaskManager.registerTask(1.hours) { HitokotoManager.fetch() }
        DEFAULT_PUSHERS.forEach {
            it.init()
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
