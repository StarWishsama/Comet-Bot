package ren.natsuyuk1.comet.network.thirdparty.arcaea

object ArcaeaHelper {
    internal val songInfo = mutableMapOf<String, String>()

    internal fun getSongNameByID(id: String): String = songInfo[id] ?: id
}
