package click.recraft.logger

import io.netty.util.CharsetUtil.UTF_8
import java.io.ByteArrayOutputStream
import java.util.logging.Level
import java.util.logging.Logger


class LoggingOutputStream(
    private val logger: Logger,
    private val level: Level
): ByteArrayOutputStream() {
    private val separator = System.getProperty("line.separator")

    override fun flush() {
        val contents: String = toString(UTF_8.name())
        super.reset()
        if (contents.isNotEmpty() && contents != separator) {
            logger.logp(level, "", "", contents)
        }
    }
}