package io.github.starwishsama.nbot

import io.github.starwishsama.nbot.objects.*
import io.github.starwishsama.nbot.objects.checkin.CheckInData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 机器人(几乎)所有数据的存放类
 * 可以直接访问数据
 * @author Nameless
 */
object BotConstants {
    var shop: List<GroupShop> = LinkedList()
    var users: List<BotUser> = LinkedList()
    var msg: List<BotLocalization> = ArrayList()
    var cfg = Config()
    var livers: List<String> = ArrayList()
    var underCovers: List<RandomResult> = LinkedList()
    var checkInCalendar = mutableMapOf<Long, CheckInData>()
    var checkInData: List<CheckInData> = LinkedList()
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
}