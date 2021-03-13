package io.github.starwishsama.comet.api.thirdparty.penguinstats.data

import com.fasterxml.jackson.annotation.JsonProperty

data class MatrixResponse(
    @JsonProperty("matrix")
    val matrix: List<DropMatrix>
)