package io.github.starwishsama.comet.tasks

interface CometPusher {
    /**
     * 首次获取前延迟的时间, 单位分钟
     */
    val delayTime: Long

    /**
     * 获取周期间隔时长, 单位分钟
     */
    val cycle: Long

    /**
     * 获取逻辑
     */
    fun retrieve()

    /**
     * 推送逻辑
     */
    fun push()
}