package ren.natsuyuk1.comet.objects.config

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.pusher.CometPushTarget
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.json.serializers.UUIDSerializer
import java.util.*

object PushTemplateConfig : PersistDataFile<MutableSet<PushTemplate>>(
    configDirectory.resolve("push_templates.json"),
    mutableSetOf(),
    json
)

@Serializable
data class PushTemplate(
    val templateName: String,
    val template: String,
    val subscribers: MutableList<CometPushTarget>,
    val url: String,
    @Serializable(with = UUIDSerializer::class)
    val token: UUID,
)
