package ren.natsuyuk1.comet.objects.keyword

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object KeyWordData : PersistDataFile<KeyWordData.Data>(
    File(configDirectory, "keywords.json"),
    Data.serializer(),
    Data()
) {
    @Serializable
    data class Data(
        val keywords: MutableList<GroupInstance> = mutableListOf()
    )

    @Serializable
    data class GroupInstance(
        val id: Long,
        val platform: CometPlatform,
        val words: MutableList<KeyWord>
    ) {
        @Serializable
        data class KeyWord(
            val pattern: String,
            val reply: MessageWrapper,
            val isRegex: Boolean = false
        )
    }

    fun addKeyWord(id: Long, platform: CometPlatform, keyword: GroupInstance.KeyWord) {
        if (!data.keywords.any { it.id == id && it.platform == platform }) {
            data.keywords.add(GroupInstance(id, platform, mutableListOf(keyword)))
        } else {
            data.keywords.find { it.id == id && it.platform == platform }?.words?.add(keyword)
        }
    }

    fun removeKeyWord(id: Long, platform: CometPlatform, keyword: String) {
        data.keywords.find { it.id == id && it.platform == platform }?.words?.removeIf { it.pattern == keyword }
    }

    fun find(id: Long, platform: CometPlatform) = data.keywords.find { it.id == id && it.platform == platform }

    fun exists(id: Long, platform: CometPlatform, keyword: String) =
        data.keywords.any { it.id == id && it.platform == platform && it.words.any { w -> w.pattern == keyword } }
}
