package io.github.starwishsama.comet.api.thirdparty.bilibili.video

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.buildMessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import moe.sdl.yabapi.data.video.VideoInfo

fun VideoInfo.toMessageWrapper(): MessageWrapper =
    buildMessageWrapper {
        addText(
            """
            $title
            | ${owner?.name}
            | ${description.limitStringSize(80)}
            | 👍 ${stat?.like} 💰 ${stat?.coin} ⭐ ${stat?.collect}
            ${if (stat?.highestRank?.let { it > 0 } == true) "| 本站最高日排行第${stat?.highestRank}名" else ""}
            """.trimIndent().removePrefix(" "))

        addPictureByURL(cover).addText("\n直达链接: https://bilibili.com/video/${bvid}")
    }