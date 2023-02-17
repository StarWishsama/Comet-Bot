package ren.natsuyuk1.comet.utils.system

import mu.KotlinLogging
import ren.natsuyuk1.comet.utils.math.NumberUtil.formatDigests
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean

private val logger = KotlinLogging.logger {}

object RuntimeUtil {
    private const val byteToMB = 1048576.0

    fun getOperatingSystemBean(): OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()

    fun getOsInfo(): String {
        val osMX = getOperatingSystemBean()
        return "${osMX.name} ${osMX.version} (${osMX.arch})"
    }

    fun getOsType(): OsType {
        val osName = getOperatingSystemBean().name

        return when {
            osName == "Mac OS X" -> OsType.MACOS
            osName.startsWith("Win") -> OsType.WINDOWS
            osName.startsWith("Linux") -> OsType.LINUX
            else -> {
                logger.error { "检测到不受支持的系统 $osName, 部分功能将被禁用." }
                OsType.UNSUPPORTED
            }
        }
    }

    fun getOsArch(): OsArch =
        when (val osArch = getOperatingSystemBean().arch) {
            "x86_64", "amd64" -> OsArch.X86_64
            "aarch64" -> OsArch.ARM64
            else -> {
                logger.error { "检测到不受支持的系统架构 $osArch, 部分功能将被禁用." }
                OsArch.UNSUPPORTED
            }
        }

    fun getOsName(): String {
        val osMX = getOperatingSystemBean()
        return osMX.name
    }

    fun getUsedMemory(): Long {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / byteToMB.toLong()
    }

    fun getMaxMemory(): Long {
        return Runtime.getRuntime().maxMemory() / byteToMB.toLong()
    }

    val jvmVersion: String? = ManagementFactory.getRuntimeMXBean().specVersion

    fun getMemoryInfo(): String {
        val memory = ManagementFactory.getMemoryMXBean()
        val heap = memory.heapMemoryUsage
        val nonHeap = memory.nonHeapMemoryUsage
        return buildString {
            append(String.format("> %-4s %-4s %-4s", "内存信息", "堆内", "堆外"))
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "初始",
                    (heap.init / byteToMB).formatDigests(),
                    (nonHeap.init / byteToMB).formatDigests(),
                ),
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "已用",
                    (heap.used / byteToMB).formatDigests(),
                    (nonHeap.used / byteToMB).formatDigests(),
                ),
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "提交",
                    (heap.committed / byteToMB).formatDigests(),
                    (nonHeap.committed / byteToMB).formatDigests(),
                ),
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "最大",
                    (heap.max / byteToMB).formatDigests(),
                    (nonHeap.max / byteToMB).formatDigests(),
                ),
            )
        }.trim()
    }
}
