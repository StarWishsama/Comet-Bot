package io.github.starwishsama.comet.objects.draw.items

import com.google.gson.annotations.SerializedName

data class PCRCharacter(override val name: String, @SerializedName("star") override val rare: Int, override val count: Int) : GachaItem()