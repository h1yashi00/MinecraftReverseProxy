package click.recraft.server

import click.recraft.logger.LoggingOutputStream
import click.recraft.logger.ProxyLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import jline.console.ConsoleReader
import java.io.PrintStream
import java.time.LocalDate
import java.util.logging.Level
import java.util.logging.Logger

fun main() {
    MinecraftProxy().start()
}

class MinecraftProxy {
    private val port          = 25566
    private val remoteHost    = "recraft.click"
    private val remotePort    = 25565
    private val timeoutSec    = 3
    companion object {
        lateinit var logger: Logger
    }


    fun start() {
        val consoleReader = ConsoleReader()
        consoleReader.expandEvents = false
//            consoleReader.addCompleter(Consol) // nocompleter
        logger = ProxyLogger("MinecraftReverseProxy", "proxy.log", consoleReader)

        System.setOut(PrintStream(LoggingOutputStream(logger, Level.INFO), true))
        logger.info("Starting Minecraft Reverse Proxy Server...")
        logger.info("${LocalDate.now()}: remoteHost-$remoteHost: remotePort-$remotePort")
        logger.info("Connection time out $timeoutSec sec")
        val bossGroup   = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        val b = ServerBootstrap()
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(MinecraftProxyInitializer(remoteHost, remotePort, timeoutSec))
            .childOption(ChannelOption.AUTO_READ, false)
        logger.info("binding/ 127.0.0.1:$port")

        val f = b.bind(port).sync()
        f.channel().closeFuture().sync()
    }
}