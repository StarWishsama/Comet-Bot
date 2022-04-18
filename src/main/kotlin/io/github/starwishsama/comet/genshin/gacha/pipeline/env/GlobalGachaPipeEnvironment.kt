package io.github.starwishsama.comet.genshin.gacha.pipeline.env

import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaResult

@kotlinx.serialization.Serializable
class GlobalGachaPipeEnvironment: PipeEnvironment() {

    private var resultRecord: ArrayList<GachaResult> = arrayListOf()

    override fun resetVariables() {
        resultRecord = arrayListOf()
    }

    fun getRecord(): ArrayList<GachaResult> = resultRecord

    fun addResult(gachaResult: GachaResult) {
        resultRecord.add(gachaResult)
        save()
    }

    companion object {

        const val GLOBAL_IDENTIFIER = "global"

        fun fromUID(uid: Long): GlobalGachaPipeEnvironment {
            return fromUID(uid, GLOBAL_IDENTIFIER)
        }


    }


}