package click.recraft.logger

import java.io.PrintWriter
import java.io.StringWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.logging.LogRecord
import java.util.logging.Formatter
import java.util.logging.Level

class ConciseFormatter : Formatter() {
    private val date: DateFormat = SimpleDateFormat(System.getProperty("net.md_5.bungee.log-date-format", "HH:mm:ss"))
    override fun format (record: LogRecord): String {
        val formatted = StringBuilder()
        formatted.append(date.format(record.millis))
        formatted.append(" [")
        appendLevel(formatted, record.level)
        formatted.append("] ")
        formatted.append(formatMessage(record))
        formatted.append('\n')
        if (record.thrown != null) {
            val writer = StringWriter()
            record.thrown.printStackTrace(PrintWriter(writer))
            formatted.append(writer)
        }
        return formatted.toString()
    }

    private fun appendLevel(builder: StringBuilder, level: Level) {
        builder.append(level.localizedName)
        return
    }
}