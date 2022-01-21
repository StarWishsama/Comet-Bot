/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.utils

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.utils.NumberUtil.formatDigests
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean


object RuntimeUtil {
    private const val byteToMB = 1048576.0

    fun forceGC() {
        val usedMemoryBefore: Long = getUsedMemory()
        System.runFinalization()
        System.gc()
        CometVariables.daemonLogger.info("GC 清理完成 (已清理 ${usedMemoryBefore - getUsedMemory()} MB)")
    }

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
            append(String.format("> %-4s %-4s %-4s", "内存信息", "堆内", "堆外"))
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "初始",
                    (head.init / byteToMB).formatDigests(),
                    (nonHead.init / byteToMB).formatDigests()
                )
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "已使用",
                    (head.used / byteToMB).formatDigests(),
                    (nonHead.used / byteToMB).formatDigests()
                )
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "提交",
                    (head.committed / byteToMB).formatDigests(),
                    (nonHead.committed / byteToMB).formatDigests()
                )
            )
            append("\n")
            append(
                String.format(
                    "| %-4s %-4s %-4s",
                    "最大",
                    (head.max / byteToMB).formatDigests(),
                    (nonHead.max / byteToMB).formatDigests()
                )
            )
        }.trim()
    }
}