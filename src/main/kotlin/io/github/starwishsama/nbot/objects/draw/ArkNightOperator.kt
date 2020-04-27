package io.github.starwishsama.nbot.objects.draw

data class ArkNightOperator(var name: String,
        var desc: String,
        var rare: Int = 0,
        var prof: String,
        var birth: String,
        var camp: String,
        var team: String,
        var tagList: List<String>,
        var obtain: List<String>,
        var race: List<String>,
        var build: List<String>)
/**
 * name : 能天使
 * desc : 优先攻击空中单位
 * rare : 6
 * prof : 狙击
 * tagList : ["输出","远程位"]
 * obtain : ["公开招募","标准寻访"]
 * birth : 拉特兰
 * camp : 企鹅物流
 * team : 企鹅物流
 * race : ["萨科塔"]
 * build : ["贸易站"]
 */