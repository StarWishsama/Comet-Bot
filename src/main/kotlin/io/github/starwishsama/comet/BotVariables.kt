package io.github.starwishsama.comet

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.starwishsama.comet.objects.BotLocalization
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.Config
import io.github.starwishsama.comet.objects.RandomResult
import io.github.starwishsama.comet.objects.draw.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.PCRCharacter
import io.github.starwishsama.comet.objects.group.Shop
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import java.util.*


/**
 * 机器人(几乎)所有数据的存放类
 * 可以直接访问数据
 * @author Nameless
 */

object BotVariables {
    var shop: List<Shop> = LinkedList()
    var users: List<BotUser> = LinkedList()
    var localMessage: List<BotLocalization> = ArrayList()
    var cfg = Config()
    var underCovers: List<RandomResult> = LinkedList()
    var cache: JsonObject = JsonObject()

    /** 明日方舟/PCR 卡池数据 */
    var arkNight: List<ArkNightOperator> = LinkedList()
    var pcr: List<PCRCharacter> = LinkedList()

    val gson: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()

    var hitokoto: Hitokoto? = null

    var switch: Boolean = true
}