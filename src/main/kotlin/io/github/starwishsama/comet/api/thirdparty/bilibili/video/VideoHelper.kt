package io.github.starwishsama.comet.api.thirdparty.bilibili.video

import io.github.starwishsama.comet.api.thirdparty.bilibili.util.buildImagePreview
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.buildMessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import moe.sdl.yabapi.data.video.VideoInfo

fun VideoInfo.toMessageWrapper(): MessageWrapper =
    buildMessageWrapper {
        addText(buildString {
            append("$title\n")
            append("| ${owner?.name}\n")
            append("| ${description.limitStringSize(80)}\n")
            append("| \uD83D\uDC4D ${stat?.like} \uD83D\uDCB0 ${stat?.coin} ⭐ ${stat?.collect}\n")
            append(if (stat?.highestRank?.let { it > 0 } == true) "| 本站最高日排行第${stat?.highestRank}名\n" else "\n")
        })

        addPictureByURL(buildImagePreview(cover))
        addText("\n直达链接: https://bilibili.com/video/${bvid}")
    }
