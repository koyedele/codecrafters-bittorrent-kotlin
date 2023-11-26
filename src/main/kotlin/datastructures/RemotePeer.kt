package datastructures

import constants.PEER_HANDSHAKE_LENGTH_BYTES
import util.Encoders
import java.io.DataInputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.net.SocketFactory

class RemotePeer(address: String) {
    private val peer: InetSocketAddress

    init {
        val (addr, port) = address.split(":")

        if (addr.isEmpty() || port.isEmpty()) {
            throw IllegalArgumentException(
                "invalid peer address \"$address\". expected address form <peer_ip>:<peer_port>"
            )
        }

        peer = InetSocketAddress(
            InetAddress.getByName(addr),
            port.toInt()
        )
    }

    fun handShake(metaInfo: MetaInfo): String {
        val message = byteArrayOf(19) +
                "BitTorrent protocol".toByteArray() +
                ByteArray(8) +
                metaInfo.infoHashBytes() +
                "00112233445566778899".toByteArray()

        val socket = SocketFactory.getDefault().createSocket()
        socket.connect(peer)

        val outputStream = socket.getOutputStream()
        outputStream.write(message)
        outputStream.flush()

        val response = ByteArray(PEER_HANDSHAKE_LENGTH_BYTES)
        DataInputStream(socket.getInputStream()).use {
            it.readFully(response)
        }

        return Encoders.hexEncode(response.copyOfRange(48, response.size))
    }
}