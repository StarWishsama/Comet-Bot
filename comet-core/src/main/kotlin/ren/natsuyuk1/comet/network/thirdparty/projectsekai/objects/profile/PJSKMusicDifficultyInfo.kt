package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.profile

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty

/**
 * Project Sekai Profile Music Data
 *
 * 有删减
 */
@Serializable
data class PJSKMusicDifficultyInfo(
    val id: Int,
    val musicId: Int,
    val musicDifficulty: MusicDifficulty,
    val playLevel: Int,
    val noteCount: Int,
    val playLevelAdjust: Double? = null,
)
