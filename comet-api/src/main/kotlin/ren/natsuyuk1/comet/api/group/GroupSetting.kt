package ren.natsuyuk1.comet.api.group

import kotlinx.coroutines.launch
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import java.io.File
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

object GroupSettingManager {
    private var scope = ModuleScope("GroupSettingManager")
    private val groupSettings = mutableSetOf<GroupSetting>()

    suspend fun init(parentContext: CoroutineContext) {
        scope = ModuleScope(scope.name(), parentContext)

        val folder = resolveDirectory("groupSettings")
        folder.touch()

        folder.listFiles()?.forEach {
            val setting = GroupSetting(it.nameWithoutExtension.toLongOrNull() ?: return@forEach)
            setting.init()
            groupSettings.add(setting)
        }
    }

    fun findGroupConfig(id: Long) = groupSettings.find { it.id == id }

    fun createGroupConfig(id: Long) = scope.launch {
        logger.info { "正在创建群 $id 的配置文件..." }

        GroupSetting(id).apply {
            groupSettings.add(this)
            init()
        }

        logger.info { "群 $id 的配置文件创建完成." }
    }
}

class GroupSetting(val id: Long) : PersistDataFile<GroupSetting.Data>(
    File(resolveDirectory("groupSettings"), "${id}.yml"),
    Data()
) {
    data class Data(
        var autoAcceptJoinRequest: Boolean = false,
        var allowRepeatMessage: Boolean = false,
    )
}
