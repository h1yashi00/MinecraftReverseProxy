package click.recraft.protocol

import click.recraft.server.DefinedPacket
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import java.lang.StringBuilder


class ReasonEncoder : MessageToByteEncoder<String>() {
    override fun encode(ctx: ChannelHandlerContext, msg: String, out: ByteBuf) {
        // disconnect reason message packet ID
        val sendPacket = ByteBufAllocator.DEFAULT.heapBuffer()
        sendPacket.writeByte(0x00)
        // ""でStringを囲まないとエラーが出力される
        val properFormat = StringBuilder().append("\"").append(msg).append("\"").toString()
        DefinedPacket.writeString(properFormat, sendPacket)

        DefinedPacket.writeVarInt(sendPacket.readableBytes(), out)
        out.writeBytes(sendPacket)
        sendPacket.release()
    }
}
