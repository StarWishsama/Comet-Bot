package io.github.starwishsama.nbot

import io.github.starwishsama.nbot.objects.*
import java.util.*

class BotConstants {
    var cfg = Config()
    var shopItems: Collection<ShopItem> = HashSet()
    var users: Collection<BotUser> = HashSet()
    var msg: List<BotLocalization> = ArrayList()
    var livers: List<String> = ArrayList()
    var underCovers: List<RandomResult> = LinkedList()
}