package click.recraft.handler

import click.recraft.protocol.ValidLoginPacket
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
        fun closeAndFlush(channel: Channel) {
            if (channel.isActive) {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
            }
        }
    }
    private lateinit var outboundChannel: Channel
    private var success = false
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val inboundChannel = ctx.channel()
        if (msg is ValidLoginPacket) {
            val packet = msg as ValidLoginPacket
            println("packet")
            println("out bound channel is ${outboundChannel.isActive}")
            packet.buf.copy().forEachByte {
                print(String.format("0x%x ", it))
                true
            }
            outboundChannel.writeAndFlush(packet.buf).addListener(object: ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    if (future.isSuccess) {
                        ctx.channel().read()
                        success = true
                        println("succcess")
                    }
                    else {
                        ctx.channel().close()
                        println("connect false")
                    }
                }
            })
        }
        else {
            if (!this::outboundChannel.isInitialized) return
            if (!success) {
                ctx.channel().disconnect()
            }
            outboundChannel.writeAndFlush(msg).addListener(object: ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    println("${future.isSuccess}")
                    if (future.isSuccess) {
                        ctx.channel().read()
                    } else {
                        future.channel().close()
                    }
                }
            })
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        if (!this::outboundChannel.isInitialized) return
        closeAndFlush(outboundChannel)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        println("removed!!")
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
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
                    println("aaaaaaa")
                    ctx.channel().read()
                }
                else {
                    println("aaaaaaaaaaaaaaaaaaaaaaaaaaa")
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
                // TODO make a disconnect handler
                println("wired packet: ${cause.message}")
            }
            else -> {
                cause.printStackTrace()
            }
        }
    }
}
