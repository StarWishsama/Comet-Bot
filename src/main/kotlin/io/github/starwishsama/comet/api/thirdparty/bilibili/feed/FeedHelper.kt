package io.github.starwishsama.comet.api.thirdparty.bilibili.feed

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.util.buildImagePreview
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.buildMessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import kotlinx.coroutines.runBlocking
import moe.sdl.yabapi.data.feed.FeedCardNode
import moe.sdl.yabapi.data.feed.FeedDescription
import moe.sdl.yabapi.data.feed.cards.ArticleCard
import moe.sdl.yabapi.data.feed.cards.BangumiCard
import moe.sdl.yabapi.data.feed.cards.CollectionCard
import moe.sdl.yabapi.data.feed.cards.ImageCard
import moe.sdl.yabapi.data.feed.cards.LiveCard
import moe.sdl.yabapi.data.feed.cards.RepostCard
import moe.sdl.yabapi.data.feed.cards.ShareCard
import moe.sdl.yabapi.data.feed.cards.TextCard
import moe.sdl.yabapi.data.feed.cards.VideoCard
import moe.sdl.yabapi.util.encoding.bv

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
        else -> buildMessageWrapper { addText("暂不支持该类型动态 [#${this@toMessageWrapper.description?.type}]") }
    }
}

fun FeedDescription.getReadableSentTime(): String = timestamp!!.toLocalDateTime().format(CometVariables.yyMMddPattern)

fun ArticleCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        addText("${description.userProfile?.info?.uname} 发布了文章\n")
        addText("⏰ ${description.getReadableSentTime()}\n\n")
        addText("${data.title}\n")
        addText("${data.content?.limitStringSize(50)}\n")
        addText("详情 > https://www.bilibili.com/read/cv/${description.dynamicId}\n")
    }

fun BangumiCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        addText("${description.userProfile?.info?.uname} 分享了番剧\n")
        addText("⏰ ${description.getReadableSentTime()}\n\n")
        addText("${apiSeasonInfo?.title}\n")
        addText("${newDesc?.limitStringSize(20)}\n")
        addText("详情 > $url")
    }

fun CollectionCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        addText("暂不支持该类型动态")
    }

fun ImageCard.toMessageWrapper(description: FeedDescription): MessageWrapper = buildMessageWrapper {
    addText("${description.userProfile?.info?.uname} 发布了动态\n")
    addText("⏰ ${description.getReadableSentTime()}\n\n")
    addText("${item?.description}\n\n")
    item?.pictures?.forEach {
        this.addPictureByURL(it.imgSrc?.let(::buildImagePreview))
    }
}

fun LiveCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        addText("${description.userProfile?.info?.uname} 开播了\n")
        addText("⏰ ${description.getReadableSentTime()}\n\n")
        addText("直播间标题 > ${livePlayInfo!!.title}\n")
        addText("直播间地址 > ${livePlayInfo!!.link}\n")
        addPictureByURL(livePlayInfo!!.cover?.let(::buildImagePreview))
    }

fun RepostCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        val originDynamic = runBlocking { DynamicApi.getDynamicById(item?.originFeedId!!.toLong()) }

        addText("${description.userProfile?.info?.uname} 分享了 ${originDynamic?.description?.userProfile?.info?.uname} 的动态\n")
        addText("⏰ ${description.getReadableSentTime()}\n\n")

        addText("${item?.content}\n\n")
        addText("转发动态详情 >\n\n")

        if (originDynamic == null) {
            addText("源动态已被删除")
        } else {
            originDynamic.toMessageWrapper().getMessageContent().forEach {
                addElement(it)
            }
        }
    }

fun TextCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        addText("${description.userProfile?.info?.uname} 发布了动态\n")
        addText("⏰ ${description.getReadableSentTime()}\n\n")
        addText("${item?.content}\n")
    }

fun VideoCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        addText("${description.userProfile?.info?.uname} 投递了视频\n")
        addText("⏰ ${description.getReadableSentTime()}\n\n")
        addText("查看 > https://www.bilibili.com/video/${aid?.bv}\n")
        addPictureByURL(pic?.let(::buildImagePreview))
    }

fun ShareCard.toMessageWrapper(description: FeedDescription): MessageWrapper =
    buildMessageWrapper {
        addText("${description.userProfile?.info?.uname} 分享了内容\n")
        addText("⏰ ${description.getReadableSentTime()}\n\n")
        addText("${vest?.content}\n\n")

        addPictureByURL(sketch?.coverUrl?.let(::buildImagePreview))
    }
