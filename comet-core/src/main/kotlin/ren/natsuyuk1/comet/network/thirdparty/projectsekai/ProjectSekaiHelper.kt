/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai

import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.message.asImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiMusicInfo
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusic
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusicDifficulty
import ren.natsuyuk1.comet.utils.datetime.format
import ren.natsuyuk1.comet.utils.math.NumberUtil.toInstant
import ren.natsuyuk1.comet.utils.time.yyMMddWithTimeZonePattern
internal suspend fun ProjectSekaiMusicInfo.toMessageWrapper() =
    buildMessageWrapper {
        val musicInfo = this@toMessageWrapper

        if (musicInfo.publishedAt.toInstant(true) > Clock.System.now()) {
            appendTextln("⚠ 该内容为未公开剧透内容")
        }

        appendElement(ProjectSekaiMusic.getMusicCover(musicInfo).asImage())
        appendLine()
        appendTextln(musicInfo.title)
        appendLine()
        appendTextln("作词 ${musicInfo.lyricist}")
        appendTextln("作曲 ${musicInfo.composer}")
        appendTextln("编曲 ${musicInfo.arranger}")
        appendTextln("上线时间 ${musicInfo.publishedAt.toInstant(true).format(yyMMddWithTimeZonePattern)}")
        appendLine()

        appendTextln("难度信息 >")
        MusicDifficulty.values().forEach {
            val diff = ProjectSekaiMusicDifficulty.getDifficulty(id, it) ?: return@forEach
            appendTextln("$it[${diff.playLevel}] | ${diff.totalNoteCount}")
        }

        trim()
    }
