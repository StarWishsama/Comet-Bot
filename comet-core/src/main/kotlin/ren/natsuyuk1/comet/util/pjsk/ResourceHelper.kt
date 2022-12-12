package ren.natsuyuk1.comet.util.pjsk

import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory

internal val pjskFolder = resolveResourceDirectory("./projectsekai")

fun getSekaiBestResourceURL(fileName: String) =
    "https://raw.githubusercontent.com/Sekai-World/sekai-master-db-diff/main/$fileName"

fun getSekaiResourceURL(fileName: String) = "https://gitlab.com/pjsekai/database/jp/-/raw/main/$fileName"

fun getSekaiMusicResourceURL(fileName: String) = "https://musics.pjsekai.moe/$fileName"
