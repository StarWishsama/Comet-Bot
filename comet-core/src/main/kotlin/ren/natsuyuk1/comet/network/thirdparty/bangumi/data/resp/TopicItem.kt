package ren.natsuyuk1.comet.network.thirdparty.bangumi.data.resp

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.network.thirdparty.bangumi.data.common.SearchType

@Serializable
abstract class TopicItem : Unique {
    abstract val zhTitle: String
    abstract val title: String

    abstract val url: String

    abstract val coverUrl: String

    abstract val info: String

    abstract val rank: Int?

    abstract val score: Double?
    abstract val voteCount: Int?
}

@Serializable
data class SearchTopicItem(
    val category: SearchType,

    override val id: Long,
    override val url: String,

    override val coverUrl: String,

    override val zhTitle: String,
    override val title: String,

    override val info: String,

    override val rank: Int?,

    override val score: Double?,
    override val voteCount: Int?
) : TopicItem()
