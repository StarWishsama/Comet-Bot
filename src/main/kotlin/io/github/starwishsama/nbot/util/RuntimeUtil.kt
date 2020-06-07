package io.github.starwishsama.nbot.util

import java.lang.management.ManagementFactory

fun getOsName(): String? {
    return System.getProperty("os.name")
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