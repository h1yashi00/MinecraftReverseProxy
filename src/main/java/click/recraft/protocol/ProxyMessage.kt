package click.recraft.protocol

import io.netty.buffer.ByteBuf
import io.netty.util.CharsetUtil
import java.net.InetSocketAddress
import java.net.SocketAddress

class ProxyMessage(
    private val remoteHost: SocketAddress,
    private val proxyHost: SocketAddress
)
{
    fun encoder(out: ByteBuf) {
        val remoteHostAddress = (remoteHost as InetSocketAddress).address.toString().removePrefix("/")
        val remoteHostPort    = (remoteHost as InetSocketAddress).port   .toString()
        val proxyHostAddress  = (proxyHost as InetSocketAddress).address .toString().removePrefix("/")
        val proxyHostPort     = (proxyHost as InetSocketAddress).port    .toString()
        out.writeCharSequence("PROXY", CharsetUtil.UTF_8)
        out.writeByte(0x20)
        out.writeCharSequence("TCP4", CharsetUtil.UTF_8)
        out.writeByte(0x20)
        out.writeCharSequence(remoteHostAddress, CharsetUtil.UTF_8)
        out.writeByte(0x20)
        out.writeCharSequence(proxyHostAddress, CharsetUtil.UTF_8)
        out.writeByte(0x20)
        out.writeCharSequence(remoteHostPort, CharsetUtil.UTF_8)
        out.writeByte(0x20)
        out.writeCharSequence(proxyHostPort, CharsetUtil.UTF_8)
        out.writeByte(0x0d)
        out.writeByte(0x0a)
    }
}