package click.recraft.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

fun main() {
    MinecraftProxy().start()
}

class MinecraftProxy {
    private val port          = 25566
    private val remoteHost    = "recraft.click"
    private val remotePort    = 25565
    private val timeoutSec    = 3

    fun start() {
        val bossGroup   = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        val b = ServerBootstrap()
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .handler(LoggingHandler(LogLevel.INFO))
            .childHandler(MinecraftProxyInitializer(remoteHost, remotePort, timeoutSec))
            .childOption(ChannelOption.AUTO_READ, false)

        val f = b.bind(port).sync()
        f.channel().closeFuture().sync()
    }
}