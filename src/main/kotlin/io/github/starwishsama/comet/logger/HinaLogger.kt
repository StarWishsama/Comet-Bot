package io.github.starwishsama.comet.logger

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class HinaLogger(
    val loggerName: String,
    val logAction: (String) -> Unit = {
        println(it)
    },
    var debugMode: Boolean = false,
    var simpleMode: Boolean = false,
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH/mm/ss")
) {
    // 时间 日志等级/日志等级缩写 logger名字 -> logger前缀 消息
    fun log(level: HinaLogLevel, message: String?, stacktrace: Throwable? = null, prefix: String = "") {
        if (!debugMode && level == HinaLogLevel.Debug) return

        var st = ""

        if (stacktrace != null) {
            st = formatStacktrace(stacktrace, null, simpleMode)
        }

        logAction(
            "${dateTimeFormatter.format(LocalDateTime.now())} ${level.internalName}/${level.simpleName} $loggerName -> $prefix $message"
                    + if (st.isNotEmpty()) "\n$st" else ""
        )
    }

    fun info(content: String?) {
        log(HinaLogLevel.Info, content)
    }

    fun info(content: String?, stacktrace: Throwable?) {
        log(HinaLogLevel.Info, content, stacktrace)
    }

    fun verbose(content: String?) {
        log(HinaLogLevel.Verbose, content)
    }

    fun verbose(content: String?, stacktrace: Throwable?) {
        log(HinaLogLevel.Verbose, content, stacktrace)
    }

    fun error(content: String?) {
        log(HinaLogLevel.Error, content)
    }

    fun error(content: String?, stacktrace: Throwable?) {
        log(HinaLogLevel.Error, content, stacktrace)
    }

    fun warning(content: String?) {
        log(HinaLogLevel.Warn, content)
    }

    fun warning(content: String?, stacktrace: Throwable?) {
        log(HinaLogLevel.Warn, content, stacktrace)
    }

    fun debug(content: String?) {
        log(HinaLogLevel.Debug, content)
    }

    fun debug(content: String?, stacktrace: Throwable?) {
        log(HinaLogLevel.Debug, content, stacktrace)
    }
}

/**
 * https://github.com/Polar-Pumpkin/ParrotX/blob/master/src/main/java/org/serverct/parrot/parrotx/utils/i18n/PLogger.java#L126
 * 快速向控制台输出错误的堆栈跟踪。
 *
 * @param exception     Throwable 类型的异常.
 * @param packageFilter 包名关键词过滤, 不需要可以填写 null.
 * @param simpleMode    不进行格式化, 直接输出默认格式的堆栈文本.
 *
 * @author Polar-Pumpkin
 */
internal fun formatStacktrace(exception: Throwable, packageFilter: String? = null, simpleMode: Boolean): String {
    if (simpleMode) return exception.stackTraceToString()

    return buildString {
        val msg = exception.localizedMessage
        append("========================= 发生了错误 =========================")
        append("异常类型 ▶")
        append(exception.javaClass.name)
        append(if (msg == null || msg.isEmpty()) "没有详细信息" else msg)
        // org.serverct.parrot.plugin.Plugin
        var currentPackage = ""
        for (elem in exception.stackTrace) {
            val key = elem.className
            var pass = true
            if (packageFilter != null) {
                pass = key.contains(packageFilter)
            }
            val nameSet = key.split("[.]").toTypedArray()
            val className = nameSet[nameSet.size - 1]
            val packageSet = arrayOfNulls<String>(nameSet.size - 2)
            System.arraycopy(nameSet, 0, packageSet, 0, nameSet.size - 2)
            val packageName = StringBuilder()
            for ((counter, nameElem) in packageSet.withIndex()) {
                packageName.append(nameElem)
                if (counter < packageSet.size - 1) {
                    packageName.append(".")
                }
            }
            if (pass) {
                if (packageName.toString() != currentPackage) {
                    currentPackage = packageName.toString()
                    append("")
                    append("包 $packageName ▶")
                }
                append("  ▶ 在类 ${className}, 方法 ${elem.methodName} (${elem.fileName}) 行 ${elem.lineNumber}")
            }
        }
        append("========================= 发生了错误 =========================")
    }
}

sealed class HinaLogLevel(val internalName: String, val simpleName: String) {
    object Verbose: HinaLogLevel("VERBOSE", "V")
    object Info: HinaLogLevel("INFO", "I")
    object Debug: HinaLogLevel("DEBUG", "D")
    object Error: HinaLogLevel("ERROR", "E")
    object Warn: HinaLogLevel("WARN", "W")
}