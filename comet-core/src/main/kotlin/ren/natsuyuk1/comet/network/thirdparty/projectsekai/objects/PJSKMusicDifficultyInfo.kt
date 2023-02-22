package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects

import kotlinx.serialization.Serializable

/**
 * Project Sekai Music Data
 */
@Serializable
data class PJSKMusicDifficultyInfo(
    val id: Int,
    val musicId: Int,
    val musicDifficulty: MusicDifficulty,
    val playLevel: Int,
    val totalNoteCount: Int,
)
