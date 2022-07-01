package click.recraft.handler

import io.netty.channel.*


class MinecraftBackendHandler(private val frontendHandler: MinecraftFrontendHandler) : ChannelInboundHandlerAdapter() {
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.channel().read()
    }
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        frontendHandler.inboundChannel.writeAndFlush(msg).addListener(ChannelFutureListener { future ->
            if (future.isSuccess) {
                ctx.channel().read()
            } else {
                future.channel().close()
            }
        })
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        frontendHandler.closeBoth()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        frontendHandler.closeBoth()
    }
}