package click.recraft.logger

import jline.console.ConsoleReader
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Erase
import java.io.IOException
import java.util.logging.Handler
import java.util.logging.LogRecord


class ColoredWriter(private val console: ConsoleReader) : Handler() {
    fun print(s: String) {
        try {
            console.print(Ansi.ansi().eraseLine(Erase.ALL).toString() + ConsoleReader.RESET_LINE + s + Ansi.ansi()
                .reset().toString())
            console.drawLine()
            console.flush()
        } catch (ex: IOException) {
        }
    }
    override fun publish(record: LogRecord?) {
        if (isLoggable(record)) {
            print(formatter.format(record))
        }
    }

    override fun flush() {
    }

    override fun close() {
    }
}