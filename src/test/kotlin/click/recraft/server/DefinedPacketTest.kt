package click.recraft.server

import io.netty.buffer.Unpooled
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DefinedPacketTest {
    @Test
    fun testReadIntVar0() {
        val expected = 0
        val buf = Unpooled.wrappedBuffer(byteArrayOf(0x00.toByte()))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }
    @Test
    fun testReadIntVar10() {
        val expected = 10
        val buf = Unpooled.wrappedBuffer(byteArrayOf(0x0a.toByte()))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }
    @Test
    fun testReadIntVar255() {
        val expected = 255
        val buf = Unpooled.wrappedBuffer(byteArrayOf(0xff.toByte(), 0x01.toByte()))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }

    @Test
    fun testReadIntVar127() {
       val expected = 127
        val buf = Unpooled.wrappedBuffer(byteArrayOf(0x7f.toByte()))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }

    @Test
    fun testReadIntVar25565() {
        val expected = 25565
        val buf = Unpooled.wrappedBuffer(byteArrayOf(
            0xdd.toByte(), 0xc7.toByte(), 0x01.toByte()
        ))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }

    @Test
    fun testReadIntVar2097151() {
        val expected = 2097151
        val buf = Unpooled.wrappedBuffer(byteArrayOf(
            0xff.toByte(), 0xff.toByte(), 0x7f.toByte()
        ))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }

    @Test
    fun testReadIntVar2147483647() {
        val expected = 2147483647
        val buf = Unpooled.wrappedBuffer(byteArrayOf(
            0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0x07.toByte()
        ))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }
    @Test
    fun testReadIntVarMinus1() {
        val expected = -1
        val buf = Unpooled.wrappedBuffer(byteArrayOf(
            0xff.toByte(), 0xff.toByte(), 0xff.toByte(),  0xff.toByte(), 0x0f.toByte()
        ))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }
    @Test
    fun testReadIntVarMinus2147483648() {
        val expected = -2147483648
        val buf = Unpooled.wrappedBuffer(byteArrayOf(
            0x80.toByte(), 0x80.toByte(), 0x80.toByte(), 0x80.toByte(), 0x08.toByte()
        ))
        assertEquals(expected, DefinedPacket.readVarInt(buf))
    }
}