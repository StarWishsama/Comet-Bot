package io.github.starwishsama.nbot

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.starwishsama.nbot.objects.BotLocalization
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.Config
import io.github.starwishsama.nbot.objects.RandomResult
import io.github.starwishsama.nbot.objects.draw.ArkNightOperator
import io.github.starwishsama.nbot.objects.draw.PCRCharacter
import io.github.starwishsama.nbot.objects.group.Shop
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * 机器人(几乎)所有数据的存放类
 * 可以直接访问数据
 * @author Nameless
 */

object BotConstants {
    var shop: List<Shop> = LinkedList()
    var users: List<BotUser> = LinkedList()
    var msg: List<BotLocalization> = ArrayList()
    var cfg = Config()
    var underCovers: List<RandomResult> = LinkedList()
    var cache: JsonObject = JsonObject()

    /** 舟游/PCR 数据 */
    var arkNight: List<ArkNightOperator> = LinkedList()
    var pcr: List<PCRCharacter> = LinkedList()

    var gson: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
}