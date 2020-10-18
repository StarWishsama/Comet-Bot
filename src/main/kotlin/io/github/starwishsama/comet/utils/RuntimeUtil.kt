package io.github.starwishsama.comet.utils

import java.lang.management.ManagementFactory

fun getOsInfo(): String {
    val osMX = ManagementFactory.getOperatingSystemMXBean()
    return "${osMX.name} ${osMX.version} (${osMX.arch})"
}

fun getUsedMemory(): Long {
    return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576
}

fun getMaxMemory(): Long {
    return Runtime.getRuntime().maxMemory() / 1048576
}

fun getJVMVersion(): String? {
    return ManagementFactory.getRuntimeMXBean().vmVersion
}