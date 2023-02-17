package ren.natsuyuk1.comet.objects.group

import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

object GroupSettingManager {
    private var scope = ModuleScope("GroupSettingManager")
    private val groupSettings = mutableSetOf<GroupSetting>()
    private val gsFolder = resolveDirectory("./config/group-setting/")

    suspend fun init(parentContext: CoroutineContext) {
        scope = ModuleScope(scope.name(), parentContext)

        if (!gsFolder.exists()) {
            gsFolder.mkdir()
        }

        gsFolder.listFiles()?.filter { it.name.endsWith("json") }?.forEach {
            try {
                groupSettings.add(json.decodeFromString(GroupSetting.serializer(), it.readTextBuffered()))
            } catch (e: Exception) {
                logger.warn(e) { "加载群配置 (${it.nameWithoutExtension}) 失败" }
            }
        }
    }

    fun findGroupConfig(id: Long, platform: CometPlatform) =
        groupSettings.find { it.id == id && it.platform == platform }

    suspend fun save() = groupSettings.forEach {
        val file = gsFolder.resolve("${it.id}-${it.platform.name}.json")

        if (!file.exists()) file.touch()

        file.writeTextBuffered(json.encodeToString(GroupSetting.serializer(), it))
    }

    fun createGroupConfig(id: Long, platform: CometPlatform) = scope.launch {
        logger.info { "正在创建群 $id 的配置文件..." }

        GroupSetting(id, platform).apply {
            groupSettings.add(this)
        }

        logger.info { "群 $id 的配置文件创建完成." }
    }

    fun removeGroupConfig(id: Long, platform: CometPlatform) {
        groupSettings.removeIf { it.id == id && it.platform == platform }
    }
}

@Serializable
data class GroupSetting(
    val id: Long,
    val platform: CometPlatform,
    var autoAcceptJoinRequest: Boolean = false,
    var allowRepeatMessage: Boolean = false,
)
