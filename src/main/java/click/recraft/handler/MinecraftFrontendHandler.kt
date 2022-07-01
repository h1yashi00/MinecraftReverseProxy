package click.recraft.handler

import click.recraft.protocol.ValidLoginPacket
import click.recraft.server.MinecraftProxy
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.handler.codec.CorruptedFrameException
import io.netty.handler.codec.DecoderException
import java.io.IOException

class MinecraftFrontendHandler(
    private val remoteHost: String,
    private val remotePort: Int
    ): ChannelInboundHandlerAdapter() {
    private var success = false
    lateinit var outboundChannel: Channel
    lateinit var inboundChannel: Channel
    fun closeBoth() {
        if (inboundChannel.isActive) {
            inboundChannel.close()
        }
        if (outboundChannel.isActive) {
            outboundChannel.close()
        }
    }
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
                        closeBoth()
                    }
                }
            })
        }
        else {
            if (!success) { closeBoth() }
            outboundChannel.writeAndFlush(msg).addListener(object: ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    if (future.isSuccess) {
                        ctx.channel().read()
                    } else {
                        closeBoth()
                    }
                }
            })
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        closeBoth()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        inboundChannel = ctx.channel()
        val b = Bootstrap()
        b.group(ctx.channel().eventLoop())
            .channel(ctx.channel().javaClass)
            .handler(MinecraftBackendHandler(this))
            .option(ChannelOption.AUTO_READ, false)
        val f = b.connect(remoteHost, remotePort)
        outboundChannel = f.channel()
        f.addListener(object: ChannelFutureListener {
            override fun operationComplete(future: ChannelFuture) {
                if (future.isSuccess) {
                    ctx.channel().read()
                }
                else {
                    MinecraftProxy.logger.severe("Bungee/Minecraft Serverが起動していません")
                    closeBoth()
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
                MinecraftProxy.logger.warning("1[${ctx.channel().remoteAddress()}] decoder exception ${cause.message}")
                MinecraftProxy.logger.warning("2[${ctx.channel().remoteAddress()}] decoder exception ${cause.stackTrace}")
            }
            is RuntimeException -> {
                MinecraftProxy.logger.warning("[${ctx.channel().remoteAddress()}] read time out")
            }
            else -> {
                cause.printStackTrace()
                return
            }
        }
    }
}
