package ren.natsuyuk1.comet.util.pjsk

import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory

internal val pjskFolder = resolveResourceDirectory("./projectsekai/")

fun getSekaiBestResourceURL(path: String) =
    "https://raw.githubusercontent.com/Sekai-World/sekai-master-db-diff/main/$path"

fun getSekaiResourceURL(path: String) = "https://gitlab.com/pjsekai/database/jp/-/raw/main/$path"

fun getSekaiMusicResourceURL(path: String) = "https://musics.pjsekai.moe/$path"

fun getCometDatabaseURL(path: String) =
    "https://raw.githubusercontent.com/StarWishsama/comet-resource-database/main/projectsekai/$path"
