package click.recraft.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class ProxyEncoder: MessageToByteEncoder<ProxyMessage>() {
    override fun encode(ctx: ChannelHandlerContext, proxyMessage: ProxyMessage, out: ByteBuf) {
        proxyMessage.encoder(out)
    }
}