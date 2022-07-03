package click.recraft.handler

import click.recraft.protocol.ProxyEncoder
import click.recraft.protocol.ProxyMessage
import click.recraft.protocol.ValidLoginPacket
import click.recraft.server.MinecraftProxy
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.handler.codec.CorruptedFrameException
import io.netty.handler.codec.DecoderException

import java.io.IOException

class MinecraftFrontendHandler(
    private val proxy: MinecraftProxy,
    ): ChannelInboundHandlerAdapter() {
    private var sendFirstPacket = false
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
            if (MinecraftProxy.useProxyProtocol) {
                val proxyMessage = ProxyMessage(
                    inboundChannel.remoteAddress(), // クライアントが接続してきたアドレスとポート
                    inboundChannel.localAddress() // プロキシサーバがマイクラサーバに接続しているポート
                )
                outboundChannel.pipeline().addFirst("ProxyEncoder", ProxyEncoder())
                outboundChannel.writeAndFlush(proxyMessage).addListener(object : ChannelFutureListener {
                    override fun operationComplete(future: ChannelFuture) {
                        outboundChannel.pipeline().remove("ProxyEncoder")
                    }
                })
            }
            outboundChannel.writeAndFlush(packet.buf).addListener(object: ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    if (future.isSuccess) {

                        if (packet.name == "") {
                            proxy.throttle.decThrottle(inboundChannel.remoteAddress()) // ping は throttleに含まれないようにする
                            MinecraftProxy.logger.info("[${inboundChannel.remoteAddress()}] has pinged this server")
                        }
                        else {
                            MinecraftProxy.logger.info("[${packet.name}|${ctx.channel().remoteAddress()}] <-> MinecraftFrontendHandler <-> MinecraftServer [${outboundChannel.remoteAddress()}]")
                        }
                        ctx.channel().read()
                        sendFirstPacket = true
                    }
                    else {
                        closeBoth()
                    }
                }
            })
        }
        else {
            if (!sendFirstPacket) { closeBoth() }

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
        val f = b.connect(proxy.minecraftHostIp, proxy.minecraftHostPort)
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
