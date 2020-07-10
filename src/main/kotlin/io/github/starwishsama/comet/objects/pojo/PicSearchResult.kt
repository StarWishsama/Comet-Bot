package io.github.starwishsama.comet.objects.pojo

data class PicSearchResult(val picUrl: String, val originalUrl: String, val similarity: Double) {
    companion object {
        fun emptyResult(): PicSearchResult {
            return PicSearchResult("", "", -1.0)
        }
    }
}