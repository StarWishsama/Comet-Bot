package io.github.starwishsama.nbot.objects

data class PicSearchResult(val picUrl: String, val originalUrl: String, val similarity: Double) {
    companion object {
        fun emptyResult(): PicSearchResult {
            return PicSearchResult("", "", 0.0)
        }
    }
}