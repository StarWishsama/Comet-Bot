package io.github.starwishsama.comet.objects.pojo

import com.google.gson.annotations.SerializedName

data class Hitokoto(
        @SerializedName("hitokoto")
        val content: String?,
        @SerializedName("from")
        val source: String?,
        @SerializedName("from_who")
        val author: String?
)