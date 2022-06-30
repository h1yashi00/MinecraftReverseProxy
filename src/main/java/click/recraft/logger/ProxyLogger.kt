package click.recraft.logger

import jline.console.ConsoleReader
import java.io.IOException
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger

class ProxyLogger(loggingName: String, filePattern: String, consoleReader: ConsoleReader): Logger(loggingName, null) {
    init {
        level = Level.ALL
        try {
            val consoleHandler = ColoredWriter(consoleReader)
            consoleHandler.level = Level.ALL
            consoleHandler.formatter = ConciseFormatter()
            addHandler(consoleHandler)

            val fileHandler = FileHandler(filePattern, 1 shl 24, 8, true)
            fileHandler.level = Level.ALL
            fileHandler.formatter = ConciseFormatter()
            addHandler(fileHandler)
        } catch ( ex: IOException )
        {
            System.err.println( "Could not register logger!" );
            ex.printStackTrace();
        }
    }
}