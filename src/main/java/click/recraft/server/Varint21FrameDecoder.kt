package click.recraft.server

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.CorruptedFrameException

class Varint21FrameDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, comeIn: ByteBuf, out: MutableList<Any>) {
        comeIn.markReaderIndex()

        val buf = ByteArray(3)
        val length: Int
        for (i in 0..2) {
            if (!comeIn.isReadable) {
                comeIn.resetReaderIndex()
                return
            }

            buf[i] = comeIn.readByte()
            if (buf[i] >= 0) {
                length = DefinedPacket.readVarInt(Unpooled.wrappedBuffer(buf))
                if (length == 0) {
                    throw CorruptedFrameException("Empty Packet!")
                }
                if (comeIn.readableBytes() < length) {
                    comeIn.resetReaderIndex()
                    return
                }
                else {
                    out.add(comeIn.slice(comeIn.readerIndex(), length).retain())
                    comeIn.skipBytes(length)
                    return
                }
            }
        }
        throw CorruptedFrameException("length wider than 21-bit")
    }
}
