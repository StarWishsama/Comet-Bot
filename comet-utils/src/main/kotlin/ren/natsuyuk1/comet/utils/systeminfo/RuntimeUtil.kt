package ren.natsuyuk1.comet.utils.systeminfo

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
                    (nonHeap.init / byteToMB).formatDigests()
                )
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "已使用",
                    (heap.used / byteToMB).formatDigests(),
                    (nonHeap.used / byteToMB).formatDigests()
                )
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "提交",
                    (heap.committed / byteToMB).formatDigests(),
                    (nonHeap.committed / byteToMB).formatDigests()
                )
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "最大",
                    (heap.max / byteToMB).formatDigests(),
                    (nonHeap.max / byteToMB).formatDigests()
                )
            )
        }.trim()
    }
}
