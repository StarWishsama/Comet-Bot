package ren.natsuyuk1.comet.network.thirdparty.bilibili.feed

import kotlinx.coroutines.runBlocking
import moe.sdl.yabapi.data.feed.FeedCardNode
import moe.sdl.yabapi.data.feed.FeedDescription
import moe.sdl.yabapi.data.feed.cards.*
import moe.sdl.yabapi.util.encoding.bv
import ren.natsuyuk1.comet.network.thirdparty.bilibili.DynamicApi
import ren.natsuyuk1.comet.network.thirdparty.bilibili.util.buildImagePreview
import ren.natsuyuk1.comet.utils.math.NumberUtil.toLocalDateTime
import ren.natsuyuk1.comet.utils.message.Image
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.comet.utils.time.yyMMddPattern

fun FeedCardNode.toMessageWrapper(): MessageWrapper {
    val description = this.description!!

    return when (val card = this.getCard()!!) {
        is ArticleCard -> card.toMessageWrapper(description)
        is BangumiCard -> card.toMessageWrapper(description)
        is CollectionCard -> card.toMessageWrapper(description)
        is ImageCard -> card.toMessageWrapper(description)
        is LiveCard -> card.toMessageWrapper(description)
        is RepostCard -> card.toMessageWrapper(description)
        is TextCard -> card.toMessageWrapper(description)
        is VideoCard -> card.toMessageWrapper(description)
        is ShareCard -> card.toMessageWrapper(description)
        else -> buildMessageWrapper { appendText("暂不支持该类型动态 [#${this@toMessageWrapper.description?.type}]") }
    }
}

fun FeedDescription.getReadableSentTime(): String = timestamp!!.toLocalDateTime().format(yyMMddPattern)

fun ArticleCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        appendText("${description.userProfile?.info?.uname} 发布了文章\n")
        appendText("⏰ ${description.getReadableSentTime()}\n\n")
        appendText("${data.title}\n")
        appendText("${data.content?.limit(50)}\n")
        appendText("详情 > https://www.bilibili.com/read/cv/${description.dynamicId}\n")
    }

fun BangumiCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        appendText("${description.userProfile?.info?.uname} 分享了番剧\n")
        appendText("⏰ ${description.getReadableSentTime()}\n\n")
        appendText("${apiSeasonInfo?.title}\n")
        appendText("${newDesc?.limit(20)}\n")
        appendText("详情 > $url")
    }

fun CollectionCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        appendText("暂不支持该类型动态")
    }

fun ImageCard.toMessageWrapper(description: FeedDescription): MessageWrapper = buildMessageWrapper {
    appendText("${description.userProfile?.info?.uname} 发布了动态\n")
    appendText("⏰ ${description.getReadableSentTime()}\n\n")
    appendText("${item?.description}\n\n")
    item?.pictures?.forEach { pic ->
        pic.imgSrc?.let {
            appendElement(Image(url = buildImagePreview(it)))
        }
    }
}

fun LiveCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        appendText("${description.userProfile?.info?.uname} 开播了\n")
        appendText("⏰ ${description.getReadableSentTime()}\n\n")
        appendText("直播间标题 > ${livePlayInfo!!.title}\n")
        appendText("直播间地址 > ${livePlayInfo!!.link}\n")
        livePlayInfo!!.cover?.let {
            appendElement(Image(url = buildImagePreview(it)))
        }
    }

fun RepostCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        val originDynamic = runBlocking { DynamicApi.getDynamic(item?.originFeedId!!.toLong()).getOrNull() }

        appendText("${description.userProfile?.info?.uname} 分享了 ${originDynamic?.description?.userProfile?.info?.uname} 的动态\n")
        appendText("⏰ ${description.getReadableSentTime()}\n\n")

        appendText("${item?.content}\n\n")
        appendText("转发动态详情 >\n\n")

        if (originDynamic == null) {
            appendText("源动态已被删除")
        } else {
            appendElements(originDynamic.toMessageWrapper().getMessageContent())
        }
    }

fun TextCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        appendText("${description.userProfile?.info?.uname} 发布了动态\n")
        appendText("⏰ ${description.getReadableSentTime()}\n\n")
        appendText("${item?.content}\n")
    }

fun VideoCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        appendText("${description.userProfile?.info?.uname} 投递了视频\n")
        appendText("⏰ ${description.getReadableSentTime()}\n\n")
        appendText("查看 > https://www.bilibili.com/video/${aid?.bv}\n")

        pic?.let {
            appendElement(Image(url = buildImagePreview(it)))
        }
    }

fun ShareCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        appendText("${description.userProfile?.info?.uname} 分享了内容\n")
        appendText("⏰ ${description.getReadableSentTime()}\n\n")
        appendText("${vest?.content}\n\n")

        sketch?.coverUrl?.let {
            appendElement(Image(url = buildImagePreview(it)))
        }
    }
