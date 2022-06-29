package click.recraft.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.*


class MinecraftBackendHandler(private val inboundChannel: Channel) : ChannelInboundHandlerAdapter() {
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.channel().read()
    }
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        inboundChannel.writeAndFlush(msg).addListener(ChannelFutureListener { future ->
            if (future.isSuccess) {
                ctx.channel().read()
            } else {
                future.channel().close()
            }
        })
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        MinecraftFrontendHandler.closeAndFlush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        MinecraftFrontendHandler.closeAndFlush()
    }
}