package click.recraft.server

import click.recraft.check.ProxyThrottle
import click.recraft.config.YamlConfig
import click.recraft.logger.LoggingOutputStream
import click.recraft.logger.ProxyLogger
import click.recraft.sql.Database
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
    Database.connect()
    MinecraftProxy().start()
}

// TODO マイクラの認証機能をデータベースと連携してできるように設定する｡
// TODO ping値をサーバに伝える方法を考える

// TODO dockerを利用して､リバースプロキシを設定できるようにする?
// TODO コネクションを確立したくないIPを本体のLinuxサーバに設定する(べき?)
// TODO Linuxのチューニング?DDOS対策? 開くポートは?

// client <- Minecraft(Reverse)Proxy -> Bungee/Spigot/NormalMinecraftServer
// MinecraftProxyはデフォルトで25565(MinecraftPortを使用します)
// Spigotサーバに割り当てるポートは25565以外の使用をおすすめします(WildPortScanやそれ以外の対策)

class MinecraftProxy {
    val debug: Boolean = false
    private val config = YamlConfig().apply {load()}
    val bindPort           = config.get("bind_port"          , 25565)
    val minecraftHostIp    = config.get("minecraft_host_ip"  , "127.0.0.1")
    val minecraftHostPort  = config.get("minecraft_host_port", 25577)
    val timeoutSec         = config.get("timeout_sec"        , 3)
    val checkDomain        = config.get("check_domain"       ,"recraft.click")

    init {
        useProxyProtocol = config.get("use_proxy_protocol", true)
    }

    private val throttleTime  = config.get("throttle_time", 1000)    // ms
    private val throttleLimit = config.get("throttle_limit",3)


    val throttle = ProxyThrottle(throttleTime = throttleTime, throttleLimit = throttleLimit)

    companion object {
        lateinit var logger: Logger
        var useProxyProtocol: Boolean = true
    }


    fun start() {
        val consoleReader = ConsoleReader()
        consoleReader.expandEvents = false
        logger = ProxyLogger("MinecraftReverseProxy", "proxy.log", consoleReader)

        System.setOut(PrintStream(LoggingOutputStream(logger, Level.INFO), true))
        logger.info("Starting Minecraft Reverse Proxy Server...")
        logger.info("${LocalDate.now()}: remoteHost-$minecraftHostIp: remotePort-$minecraftHostPort")
        logger.info("Connection time out $timeoutSec sec")
        logger.info("throttle: $throttleTime ms $throttleLimit limit")

        val bossGroup   = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        val b = ServerBootstrap()

        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(MinecraftProxyInitializer(this))
            .childOption(ChannelOption.AUTO_READ, false)
        logger.info("binding/ 127.0.0.1:$bindPort")

        val f = b.bind(bindPort).sync()
        f.channel().closeFuture().sync()
    }
}