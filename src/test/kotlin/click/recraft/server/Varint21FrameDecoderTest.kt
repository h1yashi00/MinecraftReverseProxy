package click.recraft.server

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class Varint21FrameDecoderTest {
    @Test
    fun testFrameDecodedPingPacket() {
        val buf = Unpooled.buffer().apply {
//            0x0f 0x00 0x2f 0x09 0x31 0x32 0x37 0x2e 0x30 0x2e 0x30 0x2e 0x31 0x27 0x10
//            0x01 0x01 0x00
            writeByte(0x0f)
            writeByte(0x00)
            writeByte(0x2f)
            writeByte(0x09)
            writeByte(0x31)
            writeByte(0x32)
            writeByte(0x37)
            writeByte(0x2e)
            writeByte(0x30)
            writeByte(0x2e)
            writeByte(0x30)
            writeByte(0x2e)
            writeByte(0x31)

            writeByte(0x27)
            writeByte(0x10)

            writeByte(0x01) //next state

            writeByte(0x01) // length
            writeByte(0x00) //
        }
        val input = buf.duplicate()
        val decoder = Varint21FrameDecoder().apply {
            isSingleDecode = true
        }
        val channel = EmbeddedChannel(
            decoder
        )
        assertTrue(channel.writeInbound(input.retain()))
        assertTrue(channel.finish())

        var read = channel.readInbound() as ByteBuf
        assertEquals(15, read.readableBytes())
        read.release()

        read = channel.readInbound() as ByteBuf
        assertEquals(1, read.readableBytes())
        read.release()
    }
    @Test
    fun testFrameDecodedLoginPacket() {
        val buf = Unpooled.buffer().apply {
            //0f 00 2f 09 6c 6f 63 61  6c 68 6f 73 74 b2 6e 02
            //0a 00 08 4e 61 72 69 6b  61 6b 65
            writeByte(0x0F)
            writeByte(0x00)
            writeByte(0x2f)
            writeByte(0x09)
            writeByte(0x6c)
            writeByte(0x6f)
            writeByte(0x63)
            writeByte(0x61)
            writeByte(0x6c)
            writeByte(0x68)
            writeByte(0x6f)
            writeByte(0x73)
            writeByte(0x74)
            writeByte(0xb2)
            writeByte(0x6e)
            writeByte(0x02)

            writeByte(0x0a)// packet length

            writeByte(0x00) // packet id
            writeByte(0x08) // name len
            writeByte(0x4e)
            writeByte(0x61)
            writeByte(0x72)
            writeByte(0x69)
            writeByte(0x6b)

            writeByte(0x61)
            writeByte(0x6b)
            writeByte(0x65)
        }
        val input = buf.duplicate()
        val decoder = Varint21FrameDecoder().apply {
            isSingleDecode = true
        }
        val channel = EmbeddedChannel(
            decoder,
        )
        assertTrue(channel.writeInbound(input.retain()))
        assertTrue(channel.finish())

        var read = channel.readInbound() as ByteBuf
        assertEquals(15, read.readableBytes())
        read.release()

        read = channel.readInbound() as ByteBuf
        assertEquals(10, read.readableBytes())
        read.release()
    }
}