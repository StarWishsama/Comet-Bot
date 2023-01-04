package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.Serializable

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
    val totalNoteCount: Int,
    val playLevelAdjust: Double = 0.0
)
