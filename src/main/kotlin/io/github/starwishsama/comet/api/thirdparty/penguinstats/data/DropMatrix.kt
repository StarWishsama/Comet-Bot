package io.github.starwishsama.comet.api.thirdparty.penguinstats.data

import com.fasterxml.jackson.annotation.JsonProperty

data class DropMatrix(
    /**
     * 关卡内部 ID
     */
    @JsonProperty("stageId")
    val stageID: String,
    /**
     * 物品内部 ID
     */
    @JsonProperty("itemId")
    val itemID: String,
    /**
     * 间隔时间内掉落的物品
     */
    @JsonProperty("quantity")
    val quantity: Long,
    /**
     * 在间隔时间内需要刷关卡的次数
     */
    @JsonProperty("times")
    val times: Int,
    /**
     * 该掉落开始时间, 单位毫秒
     */
    @JsonProperty("start")
    val start: Long,
    /**
     * 该掉落结束时间, 单位毫秒
     */
    @JsonProperty("end")
    val end: Long
)