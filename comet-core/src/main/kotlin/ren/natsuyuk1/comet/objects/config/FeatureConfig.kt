package ren.natsuyuk1.comet.objects.config

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object FeatureConfig : PersistDataFile<FeatureConfig.Data>(
    File(configDirectory, "feature.yml"),
    Data.serializer(),
    Data(),
    Yaml,
    readOnly = true
) {
    @Serializable
    data class Data(
        @Comment("Arcaea 相关设置")
        val arcaeaSetting: ArcaeaSetting = ArcaeaSetting(),
        @Comment("Project Sekai 相关设置")
        val projectSekaiSetting: ProjectSekaiSetting = ProjectSekaiSetting(),
        @Comment("签到相关设置")
        val signInSetting: SignInSetting = SignInSetting()
    )

    @Serializable
    data class ArcaeaSetting(
        @Comment(
            "是否启用 Arcaea 相关功能\n" +
                "启用后, 在下次启动时会自动下载并加载相关数据文件"
        )
        val enable: Boolean = false,
    )

    @Serializable
    data class ProjectSekaiSetting(
        @Comment(
            "是否启用 Project Sekai 相关功能\n" +
                "启用后, 在下次启动时会自动下载并加载相关数据文件"
        )
        val enable: Boolean = false,
        val minSimilarity: Double = 0.35
    )

    @Serializable
    data class SignInSetting(
        @Comment("最低可获取金币, 甚至可以为负")
        val minCoin: Double = 0.0,
        @Comment("最高可获取金币")
        val maxCoin: Double = 3.0,
        @Comment("最低可获得经验, 甚至可以为负")
        val minExp: Double = 0.0,
        @Comment("最高可获取经验")
        val maxExp: Double = 10.0,
        @Comment("连续签到几天后有连续签到加成奖励")
        val accumulateBonusStart: Int = 2,
        @Comment(
            """
        连续签到加成倍数
        连续签到加成计算公式: 今日签到获得金币/经验 * (一个不大于以下值的随机数 * (签到天数 - 连续签到奖励开始天数))
        且这个倍数不高于下面的最大值
        该加成同时作用于经验和金币
        """
        )
        val accumulateBonus: Double = 0.2,
        @Comment("最大连续签到加成倍数")
        val maxAccumulateBonus: Double = 1.5,
        @Comment("是否启用随机加成")
        val randomBonusSwitch: Boolean = true,
        @Comment("随机加成出现概率, 默认为 0.0001%")
        val randomBonusProbability: Double = 0.000001,
        @Comment("随机加成最低可能获得金币")
        val randomBonusMin: Double = 50.0,
        @Comment("随机加成最高可能获得金币")
        val randomBonusMax: Double = 100.0,
    )
}
