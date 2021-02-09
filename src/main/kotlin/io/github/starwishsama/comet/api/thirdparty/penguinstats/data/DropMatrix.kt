package io.github.starwishsama.comet.api.thirdparty.penguinstats.data

import com.google.gson.annotations.SerializedName

data class DropMatrix(
    /**
     * 关卡内部 ID
     */
    @SerializedName("stageId")
    val stageID: String,
    /**
     * 物品内部 ID
     */
    @SerializedName("itemId")
    val itemID: String,
    /**
     * 间隔时间内掉落的物品
     */
    @SerializedName("quantity")
    val quantity: Long,
    /**
     * 在间隔时间内需要刷关卡的次数
     */
    @SerializedName("times")
    val times: Int,
    /**
     * 该掉落开始时间, 单位毫秒
     */
    @SerializedName("start")
    val start: Long,
    /**
     * 该掉落结束时间, 单位毫秒
     */
    @SerializedName("end")
    val end: Long
)