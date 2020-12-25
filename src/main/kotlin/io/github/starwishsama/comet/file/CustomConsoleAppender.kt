package io.github.starwishsama.comet.file

import org.hydev.logger.HyLoggerConfig
import org.hydev.logger.LogLevel
import org.hydev.logger.appenders.Appender
import org.hydev.logger.format.AnsiColor
import org.hydev.logger.now
import org.hydev.logger.parseFormats

class CustomConsoleAppender : Appender()
{
    init
    {
        // Create formatter
        val defaultFormat = "&f[&5%s&f] [&1%s&f] [%s&f] %s&r".parseFormats()
        val fqcnFormat = "&f[&5%s&f] [&1%s&f] [%s&f(&e%s&f)] %s&r".parseFormats()

        formatter =
            {
                val time = HyLoggerConfig.timePattern.now()

                when (it.level)
                {
                    LogLevel.LOG -> defaultFormat.format(time, it.prefix, "${AnsiColor.GREEN}INFO", "${AnsiColor.RESET}${it.msg}")
                    LogLevel.WARNING -> defaultFormat.format(time, it.prefix, "${AnsiColor.RED}WARNING", "${AnsiColor.YELLOW}${it.msg}")
                    LogLevel.DEBUG -> fqcnFormat.format(time, it.prefix, "${AnsiColor.CYAN}DEBUG", if (!it.fqcn.contains("LogLevel")) it.fqcn else "", "${AnsiColor.CYAN}${it.msg}")
                    LogLevel.ERROR -> fqcnFormat.format(time, it.prefix, "${AnsiColor.RED}ERROR", if (!it.fqcn.contains("LogLevel")) it.fqcn else "", "${AnsiColor.RED}${it.msg}")
                }
            }
    }

    override fun logRaw(message: String) = HyLoggerConfig.colorCompatibility.log(message)
}