package click.recraft.protocol

import click.recraft.objective.UserName
import click.recraft.server.DefinedPacket
import click.recraft.server.MinecraftProxy
import click.recraft.server.URLRequest
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.util.CharsetUtil

class HandshakeDecoder(private val proxy: MinecraftProxy) : ByteToMessageDecoder() {
    private val handshakePacketID = 0x00

    // somewhat client send this packet to server when client disconnect to to the server
    private fun checkLegacyPing(buf: ByteBuf): Boolean {
        if (buf.readByte() != 0xfe.toByte()) {
            return false
        }
        if (buf.readByte() != 0x01.toByte()) {
            return false
        }
        if (buf.readByte() != 0xfa.toByte()) {
            return false
        }
        return true
    }

    // forge handshake server name packet format
    // recraft.click                                  ↓ forge added packet ↓
    // 114 101 99 114 97 102 116 46 99 108 105 99 107     0 70 77 76 0
    private fun isForgeServerPacket(nameFieldPacket: CharSequence): Boolean {
        val bytes = arrayListOf<Byte>()
        val bytesPacket = arrayListOf<Byte>()
        proxy.checkDomain.forEach {
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
        // 最初に送られてくるパケットだけ検査して,このデコーダーのを削除する
        ctx.pipeline().remove("handshake_decoder")
        val savedPacket = comeIn.copy()

        val checkLegacyCopy = comeIn.copy()
        if (checkLegacyPing(checkLegacyCopy)) {
            forceDisconnect(ctx, comeIn, "legacy ping is not support on this server", "Minecraftバージョン1.8以上を使用してください",null)
            return
        }
        checkLegacyCopy.release()

        val logStringBuilder = StringBuffer().append()
       val logPacketCopy = comeIn.copy()
       logPacketCopy.forEachByte {
           logStringBuilder.append((String.format("%x ", it)))
           true
       }
        logPacketCopy.release()

        val strBuilder = StringBuilder()
        val length = DefinedPacket.readVarInt(comeIn)
        strBuilder.append("length: $length ")

        val packetID = DefinedPacket.readVarInt(comeIn)
        strBuilder.append("packetID: $packetID ")

        if (comeIn.readableBytes() +1 == length) {
            out.add(ValidLoginPacket("vanilla", savedPacket, strBuilder.toString()))
            comeIn.clear()
            return
        }
        val slice = comeIn.slice(comeIn.readerIndex(), length)
        comeIn.skipBytes(length)

        if (packetID != handshakePacketID) {
            forceDisconnect(ctx, comeIn, "invalid packet", "このエラーが出続けるなら管理人に連絡してください $packetID", logStringBuilder)
            return
        }

        val protoVer = DefinedPacket.readVarInt(slice)
        strBuilder.append("protocolVer: $protoVer ")

        val strLen = slice.readByte().toInt()
        val serverAdd = slice.getCharSequence(slice.readerIndex(), strLen, CharsetUtil.UTF_8)
        strBuilder.append("server address: $serverAdd ")

        slice.skipBytes(strLen)
        // forge add mods packet in server address field
        if (!(serverAdd == proxy.checkDomain || isForgeServerPacket(serverAdd))) {
            forceDisconnect(ctx, comeIn, "サーバのドメインと一致しません", "直接IPアドレスを入力するのではなく､ドメイン名を入力してください", logStringBuilder)
            return
        }
        val port = DefinedPacket.readVarShort(slice)
        strBuilder.append("port: $port ")
        if (port != proxy.bindPort) {
            forceDisconnect(ctx, comeIn, "指定したポートが${proxy.bindPort}と一致しません", "{${proxy.checkDomain}}で接続もしくは${proxy.checkDomain}:${proxy.bindPort}で接続", logStringBuilder)
            return
        }
        val nextState = DefinedPacket.readVarInt(slice) // expect 0x01 or 0x02
        strBuilder.append("next state: $nextState")
        // ping
        if (nextState == responseStatus) {
            // TODO connect server
            out.add(ValidLoginPacket("", savedPacket, strBuilder.toString()))
            comeIn.clear()
        }
        // login start
        else if (nextState == responseLogin) {
            val len = slice.readByte().toInt()
            val loginSlice = comeIn.slice(comeIn.readerIndex(), len)
            if (loginSlice.readByte() != 0x00.toByte()) {
                forceDisconnect(ctx, comeIn, "invalid login packet", "このエラーが出続けるようなら管理人に連絡してください",logStringBuilder)
                return
            }
            val nameLen = loginSlice.readByte().toInt()
            val playerName = loginSlice.readCharSequence(nameLen, CharsetUtil.UTF_8).toString()

//            if (!URLRequest.checkPlayer(UserName(playerName))) {
//                forceDisconnect(ctx, comeIn, "Migrationの認証が確認できませんでした｡", "minecraft.netに接続して､スキン変更からChange Your Capeより､Migratorを選択してサーバへ再接続してください\n次回のログイン時には､マントの着用の必要はありません｡", logStringBuilder)
//                return
//            }
            out.add(ValidLoginPacket(playerName, savedPacket, strBuilder.toString()))
            comeIn.clear()
        }
        else {
            forceDisconnect(ctx, comeIn, "正しいパケットではありません", "マインクラフトのクライアントを利用して接続してください", logStringBuilder)
        }
        return
    }

    private fun forceDisconnect(ctx: ChannelHandlerContext, comeIn: ByteBuf, reason: String, solve: String, logPacket: StringBuffer?) {
        ctx.channel().writeAndFlush("接続に失敗しました｡\n\n§c理由: $reason\n\n§b解決策: $solve").addListener(object: ChannelFutureListener{
            override fun operationComplete(future: ChannelFuture) {
                future.channel().close()
                comeIn.clear()
                if (logPacket == null) {
                    return
                }
                MinecraftProxy.logger.severe("[${ctx.channel().remoteAddress()}] $reason $logPacket")
            }
        })
    }
}