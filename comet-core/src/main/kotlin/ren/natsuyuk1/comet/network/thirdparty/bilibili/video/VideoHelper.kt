package ren.natsuyuk1.comet.network.thirdparty.bilibili.video

import moe.sdl.yabapi.data.video.VideoInfo
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.bilibili.util.buildImagePreview
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.string.StringUtil.limit

fun VideoInfo.toMessageWrapper(): MessageWrapper =
    buildMessageWrapper {
        appendText(
            buildString {
                append("$title\n")
                append("| ${owner?.name}\n")
                append("| ${description.limit(80)}\n")
                val like = stat?.like?.getBetterNumber()
                val coin = stat?.coin?.getBetterNumber()
                val collect = stat?.collect?.getBetterNumber()
                append(
                    "| \uD83D\uDC4D $like \uD83D\uDCB0 $coin ⭐ $collect\n"
                )
                append(if (stat?.highestRank?.let { it > 0 } == true) "| 本站最高日排行第${stat?.highestRank}名\n" else "\n")
            }
        )

        appendElement(Image(url = buildImagePreview(cover)))
        appendText("\n直达链接: https://bilibili.com/video/$bvid")
    }
