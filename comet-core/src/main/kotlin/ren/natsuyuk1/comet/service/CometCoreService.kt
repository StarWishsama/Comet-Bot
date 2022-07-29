package ren.natsuyuk1.comet.service

import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.api.group.GroupSettingManager
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiHelper
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.hours

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
    }
}
