package io.github.starwishsama.comet.genshin.gacha.pool

import kotlinx.serialization.*
import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaResult
import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaTransitionResult
import io.github.starwishsama.comet.genshin.gacha.data.item.*
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.DestinyPipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.GlobalGachaPipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.PipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.PipeEnvironmentCache
import io.github.starwishsama.comet.genshin.utils.FileUtils
import io.github.starwishsama.comet.genshin.utils.JsonHelper

@Serializable
sealed class GachaPool: GachaPoolImpl {

    abstract val upFourStarList: List<Item>
    abstract val upFiveStarList: List<Item>

    override fun getPoolResults(uid: Long): List<GachaResult> =
        GlobalGachaPipeEnvironment.fromUID(uid).getRecord().filter { GachaPoolManager.getGachaPools().indexOf(this) == GachaPoolManager.getGachaPools().indexOf(it.pool) }

    fun getFinalResult(gachaTransitionResult: GachaTransitionResult, uid: Long, destinyIdentifier: String? = null): GachaResult {
        destinyIdentifier?.let { id -> PipeEnvironment.fromUID<DestinyPipeEnvironment>(uid, id) }?.let { env ->
            if (gachaTransitionResult.isDestiny) {
                env.weaponDestinyValue = 0
                val result = GachaResult(System.currentTimeMillis(), this, ItemPool.getItemPool().first { it.id == env.weaponDestiny })
                GlobalGachaPipeEnvironment.fromUID(uid).addResult(result)
                return result
            } else {
                val i = GachaResult(System.currentTimeMillis(), this, getFinalList(gachaTransitionResult).random())
                if (i.item.id == env.weaponDestiny) env.weaponDestinyValue =
                    0 else if (i.item.itemStar == ItemStar.FIVE) env.weaponDestinyValue++
                GlobalGachaPipeEnvironment.fromUID(uid).addResult(i)
                return i
            }
        }
        val gachaResult = GachaResult(System.currentTimeMillis(), this, getFinalList(gachaTransitionResult).random())
        GlobalGachaPipeEnvironment.fromUID(uid).addResult(gachaResult)
        return gachaResult
    }

    fun getFinalList(gachaTransitionResult: GachaTransitionResult): List<Item> {
        val finalList = { uplist: List<Item>, noUPList: List<Item> ->
            if (gachaTransitionResult.isUp) {
                uplist.filter {
                    gachaTransitionResult.type == null ||
                        it.itemType == gachaTransitionResult.type }
            } else {
                noUPList.filter {
                    gachaTransitionResult.type == null ||
                        it.itemType == gachaTransitionResult.type }
            }
        }

        return when (gachaTransitionResult.star) {
            ItemStar.FIVE -> finalList(upFiveStarList, getNoUpFiveStarList().let { it.ifEmpty { upFiveStarList } })
            ItemStar.FOUR -> finalList(upFourStarList, getNoUpFourStarList().let { it.ifEmpty { upFourStarList } })
            else -> getThreeStarList()
        }
    }

    override fun tenGacha(uid: Long): List<GachaResult> = List(10) { gacha(uid) }

    private fun getNoUpFiveStarList(): List<Item> {
        val noUPFiveStarIdList = arrayListOf(48, 49, 50, 51, 52, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81)
        return ItemPool.getItemPool().filter { noUPFiveStarIdList.contains(it.id) }
    }

    private fun getNoUpFourStarList(): List<Item> {
        val onlyUPFourStarIdList = arrayListOf(99, 101, 112, 115)
        return ItemPool.getItemPool().filter { it.itemStar == ItemStar.FOUR && !onlyUPFourStarIdList.contains(it.id) && !upFourStarList.any { up -> up.id == it.id} }
    }

    private fun getThreeStarList(): List<Item> {
        return ItemPool.getItemPool().filter { it.itemStar == ItemStar.THREE }
    }

}

object GachaPoolManager {

    private var gachaPools: ArrayList<GachaPool> = arrayListOf()

    fun init() {
        val gachaPoolsConfig = FileUtils.getGachaPool()
        if (gachaPoolsConfig.exists()) {
            gachaPools = JsonHelper.json.decodeFromString(gachaPoolsConfig.readText())
        } else {
            gachaPools = getDefaultPools()
            gachaPoolsConfig.parentFile?.mkdirs()
            gachaPoolsConfig.createNewFile()
            gachaPoolsConfig.writeText(JsonHelper.json.encodeToString(gachaPools))
        }
    }

    fun resetAllPool(uid: Long) {
        PipeEnvironmentCache.fromUID(uid).reset()
    }

    fun getAllResults(uid: Long): List<GachaResult> = GlobalGachaPipeEnvironment.fromUID(uid).getRecord()

    fun getGachaPools(): ArrayList<GachaPool> = gachaPools

    private fun getDefaultPools(): ArrayList<GachaPool> {
        val itemPool = ItemPool.getItemPool()
        val currentUpCharFourStarIdList = arrayListOf(32, 45, 110)
        val currentUpCharFiveStarIdList = arrayListOf(116)
        val currentUp2CharFiveStarIdList = arrayListOf(57)
        val currentUpWeaponFourStarIdList = arrayListOf(14, 30, 20, 25, 23)
        val currentUpWeaponFiveStarIdList = arrayListOf(117, 88)
        val upCharFourStarList = itemPool.filter { currentUpCharFourStarIdList.contains(it.id) }
        val upCharFiveStarList = itemPool.filter { currentUpCharFiveStarIdList.contains(it.id) }
        val up2CharFiveStarList = itemPool.filter { currentUp2CharFiveStarIdList.contains(it.id) }
        val upWeapFourStarList = itemPool.filter { currentUpWeaponFourStarIdList.contains(it.id) }
        val upWeapFiveStarList = itemPool.filter { currentUpWeaponFiveStarIdList.contains(it.id) }
        return arrayListOf(
            CharacterPool(upCharFourStarList, upCharFiveStarList),
            CharacterPool(upCharFourStarList, up2CharFiveStarList),
            WeaponPool(upWeapFourStarList, upWeapFiveStarList),
            PermanentPool(arrayListOf(), arrayListOf())
        )
    }

}

interface GachaPoolImpl {

    fun getPoolType(): PoolType

    fun reset(uid: Long)

    fun gacha(uid: Long): GachaResult

    fun tenGacha(uid: Long): List<GachaResult>

    fun getPoolResults(uid: Long): List<GachaResult>

}
