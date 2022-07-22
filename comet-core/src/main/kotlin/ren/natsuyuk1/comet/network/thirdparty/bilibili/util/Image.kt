package ren.natsuyuk1.comet.network.thirdparty.bilibili.util

import moe.sdl.yabapi.enums.ImageFormat
import moe.sdl.yabapi.util.string.buildImageUrl

/**
 * build image preview url for bilibili images
 */
fun buildImagePreview(url: String) =
    buildImageUrl(
        url,
        ImageFormat.JPEG,
        height = 800,
        weight = 600
    )
