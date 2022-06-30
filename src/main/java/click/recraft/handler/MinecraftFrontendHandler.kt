package click.recraft.handler

import click.recraft.protocol.ValidLoginPacket
import click.recraft.server.MinecraftProxy
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.CorruptedFrameException
import io.netty.handler.codec.DecoderException
import java.io.IOException

class MinecraftFrontendHandler(
    private val remoteHost: String,
    private val remotePort: Int
    ): ChannelInboundHandlerAdapter() {
    companion object {
        private lateinit var outboundChannel: Channel
        private lateinit var inboundChannel : Channel
        fun closeAndFlush() {
            if (this::inboundChannel.isInitialized) {
                if (inboundChannel.isActive) {
                    inboundChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
                }
            }
            if (this::outboundChannel.isInitialized) {
                if (inboundChannel.isActive) {
                    outboundChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
                }
            }
        }
    }
    private var success = false
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ValidLoginPacket) {
            val packet = msg as ValidLoginPacket
            outboundChannel.writeAndFlush(packet.buf).addListener(object: ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    if (future.isSuccess) {
                        MinecraftProxy.logger.info("[${packet.name}|${ctx.channel().remoteAddress()}] <-> MinecraftFrontendHandler <-> MinecraftServer")
                        ctx.channel().read()
                        success = true
                    }
                    else {
                        closeAndFlush()
                    }
                }
            })
        }
        else {
            if (!success) { closeAndFlush() }
            outboundChannel.writeAndFlush(msg).addListener(object: ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    if (future.isSuccess) {
                        ctx.channel().read()
                    } else {
                        closeAndFlush()
                    }
                }
            })
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        inboundChannel = ctx.channel()
        val b = Bootstrap()
        b.group(ctx.channel().eventLoop())
            .channel(ctx.channel().javaClass)
            .handler(MinecraftBackendHandler(ctx.channel()))
            .option(ChannelOption.AUTO_READ, false)
        val f = b.connect(remoteHost, remotePort)
        outboundChannel = f.channel()
        f.addListener(object: ChannelFutureListener {
            override fun operationComplete(future: ChannelFuture) {
                if (future.isSuccess) {
                    ctx.channel().read()
                }
                else {
                    closeAndFlush()
                }
            }
        })
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        when (cause) {
            is IOException -> {
                println(cause.message)
            }
            is CorruptedFrameException -> {
                println("corrupted frame: ${cause.message}")
            }
            is DecoderException -> {
                println("wired packet: ${cause.message}")
            }
            is RuntimeException -> {
                MinecraftProxy.logger.warning("[${ctx.channel().remoteAddress()}] read time out")
            }
            else -> {
                cause.printStackTrace()
            }
        }
    }
}
