package click.recraft.server

import click.recraft.handler.MinecraftFrontendHandler
import click.recraft.protocol.HandshakeDecoder
import io.netty.channel.*
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler

class MinecraftProxyInitializer(
    private val remoteHost: String,
    private val remotePort: Int,
    private val timeoutSec: Int
    ) : ChannelInitializer<SocketChannel>()  {
    override fun initChannel(ch: SocketChannel) {
        println("${ch.remoteAddress()}")
        val frontendHandler = MinecraftFrontendHandler(remoteHost, remotePort)
        ch.pipeline().addLast(frontendHandler)
        ch.pipeline().addFirst("reasonMessage_Encoder", ReasonEncoder())
        ch.pipeline().addFirst("handshake_decoder", HandshakeDecoder(frontendHandler))
        ch.pipeline().addFirst("readTimeoutHandler", ReadTimeoutHandler(timeoutSec))
    }
}