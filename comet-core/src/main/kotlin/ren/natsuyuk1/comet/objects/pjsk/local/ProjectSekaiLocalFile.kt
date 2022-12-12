package ren.natsuyuk1.comet.objects.pjsk.local

import kotlinx.datetime.Clock
import java.io.File
import kotlin.time.Duration

/**
 * 代表一个 Project Sekai: Colorful Stage 游戏数据文件
 *
 * @param file 数据文件
 * @param checkDuration 检查更新周期, 可空
 */
abstract class ProjectSekaiLocalFile(
    val file: File,
    private val checkDuration: Duration? = null
) {
    /**
     * 加载当前 Project Sekai 游戏数据
     */
    abstract suspend fun load()

    /**
     * 更新当前 Project Sekai 游戏数据
     *
     * @return 是否更新
     */
    abstract suspend fun update(): Boolean

    fun checkOutdated(): Boolean = checkDuration == null || (Clock.System.now() - getLastUpdateTime() > checkDuration)
}
