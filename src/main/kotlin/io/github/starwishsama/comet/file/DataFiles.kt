/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.file

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.config.DataFile
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.writeClassToJson
import io.github.starwishsama.comet.utils.writeString
import net.mamoe.yamlkt.Yaml
import java.io.File

object DataFiles {
    val userCfg: DataFile = DataFile(File(BotVariables.filePath, "users.json"), DataFile.FilePriority.HIGH) {
        it.writeClassToJson(BotVariables.users)
    }

    val cfgFile: DataFile = DataFile(File(BotVariables.filePath, "config.yml"), DataFile.FilePriority.HIGH) {
        it.writeString(Yaml.encodeToString(CometConfig()), isAppend = false)
    }

    val pcrData: DataFile = DataFile(File(FileUtil.getResourceFolder(), "pcr.json"), DataFile.FilePriority.NORMAL)

    val arkNightData: DataFile =
        DataFile(File(FileUtil.getResourceFolder(), "arkNights.json"), DataFile.FilePriority.NORMAL)

    val perGroupFolder: DataFile = DataFile(FileUtil.getChildFolder("groups"), DataFile.FilePriority.NORMAL) {
        it.mkdirs()
    }

    val githubRepoData: DataFile =
        DataFile(File(FileUtil.getResourceFolder(), "repos.yml"), DataFile.FilePriority.LOW) {
            it.createNewFile()
        }

    val allDataFile = listOf(
        userCfg, cfgFile, pcrData, arkNightData, perGroupFolder
    )
}