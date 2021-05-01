package io.github.starwishsama.comet.utils.math

object MathUtil {
    /**
     * 计算原百分比概率加 n 倍权值后的概率
     *
     * 例如: 主事件总数为 m 个, 对应加权事件中占 n 个, 增加权值为 x.
     *
     * n/m -> xn/m -> xn/xn+m
     *
     * @param root 主事件总数
     * @param sub 子事件总数
     * @param weightValue 权值大小
     *
     * @return 原百分比概率加 n 倍权值后的概率
     */
    fun calculateWeight(root: Int, sub: Int, weightValue: Int): Double {
        require(root > 0) { "主事件个数必须大于 0!" }
        require(sub > 0) { "子事件个数必须大于 0!" }
        require(weightValue > 0) { "权重大小必须大于 0!" }

        val afterWeight = sub * weightValue

        println(afterWeight)

        println(sub)

        return afterWeight.toDouble() / (sub + afterWeight.toDouble())
    }
}