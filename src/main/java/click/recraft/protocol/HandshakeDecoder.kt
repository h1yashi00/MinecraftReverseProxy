package click.recraft.protocol

import click.recraft.objective.UserName
import click.recraft.server.DefinedPacket
import click.recraft.server.URLRequest
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.FutureListener
import java.lang.StringBuilder

class HandshakeDecoder: ByteToMessageDecoder() {
    private val handshakePacketID = 0x00
    private val serverPort        = 25566
    private val serverAddress     = "recraft.click"

    // forge handshake server name packet format
    // recraft.click                                  ↓ forge added packet ↓
    // 114 101 99 114 97 102 116 46 99 108 105 99 107     0 70 77 76 0
    private fun isForgeServerPacket(nameFieldPacket: CharSequence): Boolean {
        val bytes = arrayListOf<Byte>()
        val bytesPacket = arrayListOf<Byte>()
        serverAddress.forEach {
            bytes.add(it.toByte())
        }
        bytes.add(0x00.toByte())
        bytes.add(0x46.toByte())
        bytes.add(0x4d.toByte())
        bytes.add(0x4c.toByte())
        bytes.add(0x00.toByte())
        nameFieldPacket.forEach {
            bytesPacket.add(it.toByte())
        }
        return bytes == bytesPacket
    }
    private val responseStatus    = 0x01
    private val responseLogin     = 0x02
    override fun decode(ctx: ChannelHandlerContext, comeIn: ByteBuf, out: MutableList<Any>) {
        val savedPacket = comeIn.copy()
       comeIn.copy().forEachByte {
           print(String.format("0x%x ", it))
           true
       }
        println()
        val strBuilder = StringBuilder()
        val length = DefinedPacket.readVarInt(comeIn)
        strBuilder.append("length: $length ")


        val packetID = DefinedPacket.readVarInt(comeIn)
        strBuilder.append("packetID: $packetID ")

        val slice = comeIn.slice(comeIn.readerIndex(), length)
        comeIn.skipBytes(length)

        if (packetID != handshakePacketID) {
            forceDisconnect(ctx, comeIn, "invalid packet")
            return
        }

        val protoVer = DefinedPacket.readVarInt(slice)
        strBuilder.append("protocolVer: $protoVer ")

        val strLen = slice.readByte().toInt()
        val serverAdd = slice.getCharSequence(slice.readerIndex(), strLen, CharsetUtil.UTF_8)
        strBuilder.append("server address: $serverAdd ")

        slice.skipBytes(strLen)
        // forge add mods packet in server address field
        if (!(serverAdd == serverAddress || isForgeServerPacket(serverAdd))) {
            println(serverAdd)
            forceDisconnect(ctx, comeIn, "Please Type Domain Name")
            return
        }
        val port = DefinedPacket.readVarShort(slice)
        strBuilder.append("port: $port ")
        if (port != serverPort) {
            forceDisconnect(ctx, comeIn, "invalid server port")
            return
        }
        val nextState = DefinedPacket.readVarInt(slice) // expect 0x01 or 0x02
        strBuilder.append("next state: $nextState")
        // ping
        if (nextState == responseStatus) {
            // TODO connect server
//            ctx.write() // connect server
        }
        // login start
        else if (nextState == responseLogin) {
            val len = slice.readByte().toInt()
            val loginSlice = comeIn.slice(comeIn.readerIndex(), len)
            if (loginSlice.readByte() != 0x00.toByte()) {
                forceDisconnect(ctx, comeIn, "invalid login packet")
                return
            }
            val nameLen = loginSlice.readByte().toInt()
            val playerName = loginSlice.readCharSequence(nameLen, CharsetUtil.UTF_8).toString()

            if (!URLRequest.checkPlayer(UserName(playerName))) {
                forceDisconnect(ctx, comeIn, "Migration capeを着用してからサーバに接続してください")
                return
            }
            out.add(ValidLoginPacket(playerName, savedPacket, strBuilder.toString()))
            ctx.pipeline().remove("handshake_decoder")
            comeIn.clear()
        }
        else {
            forceDisconnect(ctx, comeIn, "invalid login packet")
        }
        return
    }

    private fun forceDisconnect(ctx: ChannelHandlerContext, comeIn: ByteBuf, errorMsg: String) {
        ctx.channel().writeAndFlush("§c$errorMsg").addListener(object: ChannelFutureListener{
            override fun operationComplete(future: ChannelFuture) {
                println("future: ${future.isSuccess}")
                ctx.channel().disconnect()
                ctx.channel().close()
                comeIn.clear()
            }
        })
    }
}