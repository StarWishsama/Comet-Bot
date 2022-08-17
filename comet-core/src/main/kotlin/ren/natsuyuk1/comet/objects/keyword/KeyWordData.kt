package ren.natsuyuk1.comet.objects.keyword

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import java.io.File

object KeyWordData : PersistDataFile<MutableList<KeyWordData.Data>>(
    File(configDirectory, "keywords.json"),
    mutableListOf()
) {
    data class Data(
        val id: Long,
        val platform: LoginPlatform,
        val words: MutableList<KeyWord>
    ) {
        @Serializable
        data class KeyWord(
            val pattern: String,
            val reply: MessageWrapper,
            val isRegex: Boolean = false,
        )
    }

    fun addKeyWord(id: Long, platform: LoginPlatform, keyword: Data.KeyWord) {
        if (!data.any { it.id == id && it.platform == platform }) {
            data.add(Data(id, platform, mutableListOf(keyword)))
        } else {
            data.find { it.id == id && it.platform == platform }?.words?.add(keyword)
        }
    }

    fun removeKeyWord(id: Long, platform: LoginPlatform, keyword: String) {
        data.find { it.id == id && it.platform == platform }?.words?.removeIf { it.pattern == keyword }
    }

    fun find(id: Long, platform: LoginPlatform) = data.find { it.id == id && it.platform == platform }

    fun exists(id: Long, platform: LoginPlatform, keyword: String) =
        data.any { it.id == id && it.platform == platform && it.words.any { w -> w.pattern == keyword } }
}
