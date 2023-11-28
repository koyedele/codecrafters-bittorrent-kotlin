package datastructures

import constants.PEER_HANDSHAKE_LENGTH_BYTES
import util.Encoders
import java.io.DataInputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.SocketFactory

class RemotePeer {
    private val peer: InetSocketAddress
    private val socket: Socket = SocketFactory.getDefault().createSocket()

    constructor(address: String) {
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

    constructor(peerSocket: InetSocketAddress) {
        peer = peerSocket
    }

    fun handShake(metaInfo: MetaInfo): String {
        socket.connect(peer)

        val message = byteArrayOf(19) +
                "BitTorrent protocol".toByteArray() +
                ByteArray(8) +
                metaInfo.infoHashBytes() +
                "00112233445566778899".toByteArray()

        val outputStream = socket.getOutputStream()
        outputStream.write(message)
        outputStream.flush()

        val response = ByteArray(PEER_HANDSHAKE_LENGTH_BYTES)
        val inputStream = DataInputStream(socket.getInputStream())
        inputStream.readFully(response)

        return Encoders.hexEncode(response.copyOfRange(48, response.size))
    }

    fun close() {
        if (!socket.isClosed) {
            socket.close()
        }
    }

    override fun toString(): String {
        return "${peer.hostString}:${peer.port}"
    }
}