package io.github.starwishsama.comet.api.thirdparty.bgm.data.resp

import kotlinx.serialization.Serializable

/**
 * 简略剧集信息
 * @property id bangumi id
 * @property title 短标题如 "01" "500" "120" "11.5" 等
 * @property longTitle 长标题, 如 "ep.11.5 持つべきものは"
 * @property url bangumi 单集 url 如 "https://bangumi.tv/ep/183486"
 * @property category 单集类型 如 SP OP ED 等, 默认为 null, 即普通正片
 */
@Serializable
abstract class Episode : Unique {
  abstract val title: String
  abstract val longTitle: String
  abstract val url: String
  abstract val category: String?
}

@Serializable
data class SimpleEpisode(
  override val id: Long,
  override val title: String,
  override val longTitle: String,
  override val url: String,
  override val category: String? = null,
) : Episode()

@Serializable
data class FullEpisode(
  override val id: Long,
  override val title: String,
  override val longTitle: String,
  val rawDuration: String?,
  val rawDate: String?,
  val comment: Int,
  override val url: String,
  override val category: String? = null,
) : Episode()
