package ren.natsuyuk1.comet.api.group

import kotlinx.coroutines.launch
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import java.io.File
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

object GroupSettingManager {
    private var scope = ModuleScope("GroupSettingManager")
    private val groupSettings = mutableSetOf<GroupSetting>()

    suspend fun init(parentContext: CoroutineContext) {
        scope = ModuleScope(scope.name(), parentContext)

        val folder = resolveDirectory("./group-setting")

        if (!folder.exists()) {
            folder.mkdir()
        }

        folder.listFiles()?.forEach {
            // FIXME
            val setting = GroupSetting(it.nameWithoutExtension.toLongOrNull() ?: return@forEach, LoginPlatform.TEST)
            setting.init()
            groupSettings.add(setting)
        }
    }

    fun findGroupConfig(id: Long) = groupSettings.find { it.id == id }

    suspend fun save() = groupSettings.forEach { it.save() }

    fun createGroupConfig(id: Long, platform: LoginPlatform) = scope.launch {
        logger.info { "正在创建群 $id 的配置文件..." }

        GroupSetting(id, platform).apply {
            groupSettings.add(this)
            init()
        }

        logger.info { "群 $id 的配置文件创建完成." }
    }

    fun removeGroupConfig(id: Long, platform: LoginPlatform) {
        groupSettings.removeIf { it.id == id && it.platform == platform }
    }
}

class GroupSetting(val id: Long, val platform: LoginPlatform) : PersistDataFile<GroupSetting.Data>(
    File(resolveDirectory("./group-setting"), "$id.yml"),
    Data(id, platform)
) {
    data class Data(
        val id: Long,
        val platform: LoginPlatform,
        var autoAcceptJoinRequest: Boolean = false,
        var allowRepeatMessage: Boolean = false
    )
}
