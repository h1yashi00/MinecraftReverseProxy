package click.recraft.protocol

import io.netty.buffer.ByteBuf

data class ValidLoginPacket(val name: String, val buf: ByteBuf, val loginPacketInfo: String) {
}