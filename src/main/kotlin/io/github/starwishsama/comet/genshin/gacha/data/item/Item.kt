package io.github.starwishsama.comet.genshin.gacha.data.item

import io.github.starwishsama.comet.genshin.gacha.pool.GachaPool
import io.github.starwishsama.comet.genshin.gacha.pool.GachaPoolManager
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import io.github.starwishsama.comet.genshin.utils.FileUtils
import io.github.starwishsama.comet.genshin.utils.JsonHelper

object ItemFastSerializer : KSerializer<Item> {

    private val pool = ItemPool.getItemPool()

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ItemSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Item {
        val id = decoder.decodeInt()
        return pool.first { it.id == id }
    }

    override fun serialize(encoder: Encoder, value: Item) {
        encoder.encodeInt(value.id)
    }

}

object PoolFastSerializer : KSerializer<GachaPool> {

    private val pools = GachaPoolManager.getGachaPools()

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ItemSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): GachaPool {
        val id = decoder.decodeInt()
        require(id < pools.size && id >= 0)
        return pools[id]
    }

    override fun serialize(encoder: Encoder, value: GachaPool) {
        val id = pools.indexOf(value)
        require(id != -1)
        encoder.encodeInt(id)
    }

}

@Serializable
data class Item(
    val id: Int,
    val itemName: String,
    val itemType: ItemType,
    val itemSubType: ItemSubType,
    val itemStar: ItemStar,
) {
    override fun toString(): String {
        return "${itemStar.star}* $itemName"
    }
}

object ItemPool {

    fun getItemPool(): ArrayList<Item> {
        val itemPoolConfig = FileUtils.getItemPool()
        return if (itemPoolConfig.exists()) {
            JsonHelper.json.decodeFromString(itemPoolConfig.readText())
        } else {
            val itemPool = getDefaultItemList()
            itemPoolConfig.parentFile?.mkdirs()
            itemPoolConfig.createNewFile()
            itemPoolConfig.writeText(JsonHelper.json.encodeToString(itemPool))
            itemPool
        }
    }

    fun getItemFromId(id: Int): Item = getItemPool().first { it.id == id }

    private fun getDefaultItemList(): ArrayList<Item> = arrayListOf(
        Item(1, "弹弓", ItemType.WEAPON, ItemSubType.Bow, ItemStar.THREE),
        Item(2, "神射手之誓", ItemType.WEAPON, ItemSubType.Bow, ItemStar.THREE),
        Item(3, "鸦羽弓", ItemType.WEAPON, ItemSubType.Bow, ItemStar.THREE),
        Item(4, "翡玉法球", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.THREE),
        Item(5, "讨龙英杰谭", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.THREE),
        Item(6, "魔导绪论", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.THREE),
        Item(7, "黑缨枪", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.THREE),
        Item(8, "以理服人", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.THREE),
        Item(9, "沐浴龙血的剑", ItemType.WEAPON, ItemSubType.Sword, ItemStar.THREE),
        Item(10, "铁影阔剑", ItemType.WEAPON, ItemSubType.Sword, ItemStar.THREE),
        Item(11, "飞天御剑", ItemType.WEAPON, ItemSubType.Sword, ItemStar.THREE),
        Item(12, "黎明神剑", ItemType.WEAPON, ItemSubType.Sword, ItemStar.THREE),
        Item(13, "冷刃", ItemType.WEAPON, ItemSubType.Sword, ItemStar.THREE),
        Item(14, "弓藏", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FOUR),
        Item(15, "祭礼弓", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FOUR),
        Item(16, "绝弦", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FOUR),
        Item(17, "西风猎弓", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FOUR),
        Item(18, "昭心", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FOUR),
        Item(19, "祭礼残章", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FOUR),
        Item(20, "流浪乐章", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FOUR),
        Item(21, "西风秘典", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FOUR),
        Item(22, "西风长枪", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FOUR),
        Item(23, "匣里灭辰", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FOUR),
        Item(24, "雨裁", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FOUR),
        Item(25, "祭礼大剑", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FOUR),
        Item(26, "钟剑", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FOUR),
        Item(27, "西风大剑", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FOUR),
        Item(28, "匣里龙吟", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FOUR),
        Item(29, "祭礼剑", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FOUR),
        Item(30, "笛剑", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FOUR),
        Item(31, "西风剑", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FOUR),
        Item(32, "砂糖", ItemType.CHARACTER, ItemSubType.Anemo, ItemStar.FOUR),
        Item(33, "菲谢尔", ItemType.CHARACTER, ItemSubType.Electro, ItemStar.FOUR),
        Item(34, "芭芭拉", ItemType.CHARACTER, ItemSubType.Hydro, ItemStar.FOUR),
        Item(35, "烟绯", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FOUR),
        Item(36, "罗莎莉亚", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FOUR),
        Item(37, "辛焱", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FOUR),
        Item(38, "迪奥娜", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FOUR),
        Item(39, "重云", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FOUR),
        Item(40, "诺艾尔", ItemType.CHARACTER, ItemSubType.Geo, ItemStar.FOUR),
        Item(41, "班尼特", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FOUR),
        Item(42, "凝光", ItemType.CHARACTER, ItemSubType.Geo, ItemStar.FOUR),
        Item(43, "行秋", ItemType.CHARACTER, ItemSubType.Hydro, ItemStar.FOUR),
        Item(44, "北斗", ItemType.CHARACTER, ItemSubType.Electro, ItemStar.FOUR),
        Item(45, "香菱", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FOUR),
        Item(46, "雷泽", ItemType.CHARACTER, ItemSubType.Electro, ItemStar.FOUR),
        Item(47, "早柚", ItemType.CHARACTER, ItemSubType.Anemo, ItemStar.FOUR),
        Item(48, "刻晴", ItemType.CHARACTER, ItemSubType.Electro, ItemStar.FIVE),
        Item(49, "莫娜", ItemType.CHARACTER, ItemSubType.Hydro, ItemStar.FIVE),
        Item(50, "七七", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FIVE),
        Item(51, "迪卢克", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FIVE),
        Item(52, "琴", ItemType.CHARACTER, ItemSubType.Anemo, ItemStar.FIVE),
        Item(53, "宵宫", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FIVE),
        Item(54, "神里绫华", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FIVE),
        Item(55, "枫原万叶", ItemType.CHARACTER, ItemSubType.Anemo, ItemStar.FIVE),
        Item(56, "达达利亚", ItemType.CHARACTER, ItemSubType.Hydro, ItemStar.FIVE),
        Item(57, "温迪", ItemType.CHARACTER, ItemSubType.Anemo, ItemStar.FIVE),
        Item(58, "胡桃", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FIVE),
        Item(59, "可莉", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FIVE),
        Item(60, "优菈", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FIVE),
        Item(61, "魈", ItemType.CHARACTER, ItemSubType.Anemo, ItemStar.FIVE),
        Item(62, "钟离", ItemType.CHARACTER, ItemSubType.Geo, ItemStar.FIVE),
        Item(63, "阿贝多", ItemType.CHARACTER, ItemSubType.Geo, ItemStar.FIVE),
        Item(64, "甘雨", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FIVE),
        Item(70, "飞雷之弦振", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FIVE),
        Item(71, "雾切之回光", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FIVE),
        Item(72, "天空之刃", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FIVE),
        Item(73, "狼的末路", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FIVE),
        Item(74, "阿莫斯之弓", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FIVE),
        Item(75, "天空之卷", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FIVE),
        Item(76, "天空之傲", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FIVE),
        Item(77, "风鹰剑", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FIVE),
        Item(78, "和璞鸢", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FIVE),
        Item(79, "四风原典", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FIVE),
        Item(80, "天空之翼", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FIVE),
        Item(81, "天空之脊", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FIVE),
        Item(82, "尘世之锁", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FIVE),
        Item(83, "无工之剑", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FIVE),
        Item(84, "贯虹之槊", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FIVE),
        Item(85, "斫峰之刃", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FIVE),
        Item(86, "磐岩结绿", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FIVE),
        Item(87, "护摩之杖", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FIVE),
        Item(88, "终末嗟叹之诗", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FIVE),
        Item(89, "松籁响起之时", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FIVE),
        Item(90, "雷电将军", ItemType.CHARACTER, ItemSubType.Electro, ItemStar.FIVE),
        Item(91, "九条裟罗", ItemType.CHARACTER, ItemSubType.Electro, ItemStar.FOUR),
        Item(92, "薙草之稻光", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FIVE),
        Item(93, "珊瑚宫心海", ItemType.CHARACTER, ItemSubType.Hydro, ItemStar.FIVE),
        Item(94, "不灭月华", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FIVE),
        Item(96, "埃洛伊", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FIVE),
        Item(98, "托马", ItemType.CHARACTER, ItemSubType.Pyro, ItemStar.FOUR),
        Item(99, "断浪长鳍", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FOUR), // Only for UP
        Item(101, "曚云之月", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FOUR), // Only for UP
        Item(104, "荒泷一斗", ItemType.CHARACTER, ItemSubType.Geo, ItemStar.FIVE),
        Item(105, "五郎", ItemType.CHARACTER, ItemSubType.Geo, ItemStar.FOUR),
        Item(107, "赤角石溃杵", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FIVE),
        Item(109, "申鹤", ItemType.CHARACTER, ItemSubType.Cryo, ItemStar.FIVE),
        Item(110, "云堇", ItemType.CHARACTER, ItemSubType.Geo, ItemStar.FOUR),
        Item(111, "息灾", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FIVE),
        Item(112, "千岩长枪", ItemType.WEAPON, ItemSubType.Polearm, ItemStar.FOUR), // Only for UP
        Item(113, "八重神子", ItemType.CHARACTER, ItemSubType.Electro, ItemStar.FIVE),
        Item(114, "神乐之真意", ItemType.WEAPON, ItemSubType.Catalyst, ItemStar.FIVE),
        Item(115, "恶王丸", ItemType.WEAPON, ItemSubType.Claymore, ItemStar.FOUR), // Only for UP
        Item(116, "神里绫人", ItemType.CHARACTER, ItemSubType.Hydro, ItemStar.FIVE),
        Item(117, "波乱月白经津", ItemType.WEAPON, ItemSubType.Sword, ItemStar.FIVE),
        Item(118, "夜兰", ItemType.CHARACTER, ItemSubType.Hydro, ItemStar.FIVE),
        Item(119, "久歧忍", ItemType.CHARACTER, ItemSubType.Electro, ItemStar.FOUR),
        Item(120, "若水", ItemType.WEAPON, ItemSubType.Bow, ItemStar.FIVE)
    )

}