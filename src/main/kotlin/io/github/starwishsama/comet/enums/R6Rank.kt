package io.github.starwishsama.comet.enums

enum class R6Rank(val rankName: String, private val rankId: Int) {
    UNRANKED("未定级", 0), COPPER_4("紫铜 4", 1), COPPER_3("紫铜 3", 2), COPPER_2("紫铜 2", 3), COPPER_1(
        "紫铜 1",
        4
    ),
    BRONZE_4("青铜 4", 5), BRONZE_3("青铜 3", 6), BRONZE_2("青铜 2", 7), BRONZE_1("青铜 1", 8), SILVER_4(
        "白银 4",
        9
    ),
    SILVER_3("白银 3", 10), SILVER_2("白银 2", 11), SILVER_1("白银 1", 12), GOLD_4("黄金 4", 13), GOLD_3(
        "黄金 3",
        14
    ),
    GOLD_2("黄金 2", 15), GOLD_1("黄金 1", 16), PLATINUM_3("白金 3", 17), PLATINUM_2("白金 2", 18), PLATINUM_1(
        "白金 1",
        19
    ),
    DIAMOND("钻石 1", 20), CHAMPION("冠军", 21);

    companion object {
        fun getRank(id: Int): R6Rank {
            for (current in values()) {
                if (current.rankId == id) {
                    return current
                }
            }
            throw IllegalArgumentException("找不到对应的段位名")
        }
    }

}