package click.recraft.sql

import click.recraft.server.MinecraftProxy
import java.sql.Connection
import java.sql.DriverManager

object Database {
    private const val url = "jdbc:mysql://localhost/sample_db?useSSL=false"
    private const val user = "root"
    private const val password = "root"
    private lateinit var con: Connection

    fun connect() {
        con = DriverManager.getConnection(url, user, password)
    }

    fun getPlayer(uuid: String): Boolean {
        if (!this::con.isInitialized) {
            MinecraftProxy.logger.info("first database class must connect database!")
            return false
        }
        val stat = con.createStatement()
        val sql = "select * from playerdata where player_uuid = \'$uuid\'"
        val result = stat.executeQuery(sql)
        if (!result.next()) {
            return false
        }
        stat.close()
        return true
    }

    fun close() {
        con.close()
    }
}