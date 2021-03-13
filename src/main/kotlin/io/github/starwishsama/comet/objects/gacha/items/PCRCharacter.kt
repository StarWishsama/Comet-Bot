package io.github.starwishsama.comet.objects.gacha.items

import com.fasterxml.jackson.annotation.JsonProperty


data class PCRCharacter(
    override val name: String,
    @JsonProperty("star")
    override val rare: Int,
    override val count: Int
) : GachaItem()