package click.recraft.server

import click.recraft.check.ProxyThrottle
import click.recraft.logger.LoggingOutputStream
import click.recraft.logger.ProxyLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.haproxy.HAProxyCommand
import io.netty.handler.codec.haproxy.HAProxyMessage
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol
import jline.console.ConsoleReader
import java.io.PrintStream
import java.net.InetSocketAddress
import java.time.LocalDate
import java.util.logging.Level
import java.util.logging.Logger

fun main() {
    MinecraftProxy().start()
}

// TODO configから変更できるように変更｡expect domain Name を追加する｡
// TODO リバースプロキシをバンジーコードに伝えるための方法HaProxyEncoderを利用する｡
// TODO ping値をサーバに伝える方法を考える
// TODO マイクラの認証機能をデータベースと連携してできるように設定する｡
// TODO dockerを利用して､リバースプロキシを設定できるようにする?
// TODO コネクションを確立したくないIPを本体のLinuxサーバに設定する(べき?)
// TODO Linuxのチューニング?DDOS対策? 開くポートは?

class MinecraftProxy {
    private val port          = 25566
    private val remoteHost    = "127.0.0.1"
    private val remotePort    = 25577
    private val timeoutSec    = 3
    private val throttleTime  = 1000    // ms
    private val throttleLimit = 3

    val throttle = ProxyThrottle(throttleTime = throttleTime, throttleLimit = throttleLimit)
    companion object {
        lateinit var logger: Logger
        val proxy = true
    }


    fun start() {
        val consoleReader = ConsoleReader()
        consoleReader.expandEvents = false
        logger = ProxyLogger("MinecraftReverseProxy", "proxy.log", consoleReader)

        System.setOut(PrintStream(LoggingOutputStream(logger, Level.INFO), true))
        logger.info("Starting Minecraft Reverse Proxy Server...")
        logger.info("${LocalDate.now()}: remoteHost-$remoteHost: remotePort-$remotePort")
        logger.info("Connection time out $timeoutSec sec")
        logger.info("throttle: $throttleTime ms $throttleLimit limit")

        val bossGroup   = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        val b = ServerBootstrap()

        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(MinecraftProxyInitializer(this, remoteHost, remotePort, timeoutSec))
            .childOption(ChannelOption.AUTO_READ, false)
        logger.info("binding/ 127.0.0.1:$port")

        val f = b.bind(port).sync()
        f.channel().closeFuture().sync()
    }
}