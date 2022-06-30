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
        ch.pipeline().addLast("readTimeoutHandler", ReadTimeoutHandler(timeoutSec))
        ch.pipeline().addLast("handshake_decoder", HandshakeDecoder())
        ch.pipeline().addLast("reasonMessage_Encoder", ReasonEncoder())
        ch.pipeline().addLast(MinecraftFrontendHandler(remoteHost, remotePort))
    }
}