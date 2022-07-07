package click.recraft.sql

import click.recraft.tesutil.TimeCheck
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DatabaseTest {
    @Test
    fun howLongTimesDatabaseConnectionTake() {
        Database.connect()
        val timeCheck = TimeCheck().apply{start()}
        Database.getPlayer("14687d5d039949dbb3023cce4f47bc86")
        timeCheck.fin()
    }
    @Test
    fun checkPlayerUUID() {
        Database.connect()
        // Narikake
        assertTrue(Database.getPlayer("14687d5d039949dbb3023cce4f47bc86"))
        Database.close()
    }
}