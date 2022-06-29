package click.recraft.server

import io.netty.buffer.ByteBuf
import io.netty.util.CharsetUtil
import kotlin.experimental.and


object DefinedPacket {
    fun readVarInt(input: ByteBuf): Int {
        return readVarInt( input, 5 )
    }
    private fun readVarInt(input: ByteBuf, maxBytes: Int): Int {
        var out = 0
        var bytes = 0
        while (true) {
            val into = input.readByte()
            // 最初はゼロシフト
            // 最上位Bitだけ取得
            out = out or ((into and 0x7F).toInt() shl (bytes++ * 7))
            if (bytes > maxBytes) {
                throw RuntimeException("VarInt to big")
            }
            if ( into and 0x80.toByte() != 0x80.toByte()) {
                break
            }
        }
        return out
    }
    fun readVarShort(buf: ByteBuf): Int {
        var low = buf.readUnsignedShort()
        var high = 0
        if (low and 0x8000 != 0) {
            low = low and 0x7FFF
            high = buf.readUnsignedByte().toInt()
        }
        return high and 0xFF shl 15 or low
    }

    fun writeVarInt(value: Int, output: ByteBuf) {
        var value = value
        var part: Int
        while (true) {
            part = value and 0x7F
            value = value ushr 7
            if (value != 0) {
                part = part or 0x80
            }
            output.writeByte(part)
            if (value == 0) {
                break
            }
        }
    }


    fun writeString(s: String, buf: ByteBuf) {
        if (s.length > Short.MAX_VALUE) {
            throw OverflowPacketException("Cannot send string longer than Short.MAX_VALUE (got " + s.length + " characters)")
        }
        val b = s.toByteArray(CharsetUtil.UTF_8)
        writeVarInt(b.size, buf)
        buf.writeBytes(b)
    }
}