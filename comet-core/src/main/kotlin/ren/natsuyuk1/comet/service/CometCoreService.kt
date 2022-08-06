package ren.natsuyuk1.comet.service

import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.group.GroupSettingManager
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometPersistDataFile
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiHelper
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object CometCoreService {
    var scope = ModuleScope("comet-core-service")

    fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("comet-core-service", parentContext)

        scope.launch {
            initCoreService()
        }
    }

    private suspend fun initCoreService() {
        ProjectSekaiHelper.init(scope.coroutineContext)
        GroupSettingManager.init(scope.coroutineContext)
        TaskManager.registerTask(1.hours) { HitokotoManager.fetch() }
        startAutoSaveService()
    }

    private fun startAutoSaveService() {
        TaskManager.registerTaskDelayed(CometGlobalConfig.data.dataSaveDuration.minutes) {
            cometPersistDataFile.forEach {
                it.save()
                GroupSettingManager.save()
            }
        }
    }
}
