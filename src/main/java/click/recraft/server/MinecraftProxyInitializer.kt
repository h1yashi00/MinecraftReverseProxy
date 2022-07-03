package click.recraft.server

import click.recraft.handler.MinecraftFrontendHandler
import click.recraft.protocol.HandshakeDecoder
import click.recraft.protocol.ReasonEncoder
import io.netty.channel.*
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler

class MinecraftProxyInitializer(
    private val proxy: MinecraftProxy,
) : ChannelInitializer<SocketChannel>()  {
    override fun initChannel(ch: SocketChannel) {
        if (proxy.throttle.incThrottle(ch.remoteAddress())) {
            MinecraftProxy.logger.warning("${ch.remoteAddress().address}")
            ch.close() // channel
        }
        val frontendHandler = MinecraftFrontendHandler(proxy)
        ch.pipeline().addLast(frontendHandler)
        ch.pipeline().addFirst("reasonMessage_Encoder", ReasonEncoder())
        ch.pipeline().addFirst("handshake_decoder", HandshakeDecoder(proxy))
        ch.pipeline().addFirst("readTimeoutHandler", ReadTimeoutHandler(proxy.timeoutSec))
    }
}