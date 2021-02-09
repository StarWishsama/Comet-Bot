package io.github.starwishsama.comet.api.thirdparty.penguinstats.data

import com.google.gson.annotations.SerializedName

data class MatrixResponse(
    @SerializedName("matrix")
    val matrix: List<DropMatrix>
)