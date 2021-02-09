package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.utils.NumberUtil.formatDigests
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean


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

    fun getJVMVersion(): String? {
        return ManagementFactory.getRuntimeMXBean().specVersion
    }

    fun getMemoryInfo(): String {
        val memory = ManagementFactory.getMemoryMXBean()
        val head = memory.heapMemoryUsage
        val nonHead = memory.nonHeapMemoryUsage
        return buildString {
            append(String.format("%-7s | %-7s | %-7s | %-7s | %-7s", "内存情况", "初始", "使用", "提交", "最大"))
            append("\n")
            append(
                String.format("%-7s | %-7s | %-7s | %-7s | %-7s",
                    "堆内",
                    (head.init / byteToMB).formatDigests(),
                    (head.used / byteToMB).formatDigests(),
                    (head.committed / byteToMB).formatDigests(),
                    (head.max / byteToMB).formatDigests()
                )
            )
            append("\n")
            append(
                String.format("%-7s | %-7s | %-7s | %-7s | %-7s",
                    "堆外",
                    (nonHead.init / byteToMB).formatDigests(),
                    (nonHead.used / byteToMB).formatDigests(),
                    (nonHead.committed / byteToMB).formatDigests(),
                    (nonHead.max / byteToMB).formatDigests()
                )
            )
        }.trim()
    }
}