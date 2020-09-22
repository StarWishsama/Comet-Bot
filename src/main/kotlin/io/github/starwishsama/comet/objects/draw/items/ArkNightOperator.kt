package io.github.starwishsama.comet.objects.draw.items

/**
 * 明日方舟干员
 */
data class ArkNightOperator(
        override val name: String,
        val des: String,
        override val rare: Int = 0,
        val position: String,
        val birthplace: String,
        val camp: String,
        val team: String,
        val tag: List<String>,
        val approach: List<String>,
        val race: List<String>,
        override val count: Int,
) : GachaItem()

/**
"name": "森蚺",
"position": "近战位",
"en": "Eunectes",
"sex": "女",
"tag": [
"输出",
"生存",
"防护"
],
"race": [
"斐迪亚"
],
"rare": 6,
"class": "重装",
"approach": [
"标准寻访"
],
"camp": "萨尔贡",
"team": "无团队",
"des": "嘉维尔的劲敌森蚺，与“暴躁铁皮”一起踏上战场。",
"feature": "只有阻挡敌人时才能够回复技力",
"str": "优良",
"flex": "标准",
"tolerance": "优良",
"plan": "普通",
"skill": "标准",
"adapt": "标准",
"moredes": "比起拳头，她更喜欢用机器解决问题。",
"icon": "//prts.wiki/images/7/74/%E5%A4%B4%E5%83%8F_%E6%A3%AE%E8%9A%BA.png",
"half": "//prts.wiki/images/thumb/4/4a/%E5%8D%8A%E8%BA%AB%E5%83%8F_%E6%A3%AE%E8%9A%BA_1.png/110px-%E5%8D%8A%E8%BA%AB%E5%83%8F_%E6%A3%AE%E8%9A%BA_1.png",
"oriHp": "1882",
"oriAtk": "462",
"oriDef": "247",
"oriRes": "0",
"oriDt": "70s",
"oriDc": "29→31→33",
"oriBlock": "1",
"oriCd": "1.6s",
"index": "SG03",
"sort_id": "157",
"jp": "",
"birthplace": "萨尔贡"
 */