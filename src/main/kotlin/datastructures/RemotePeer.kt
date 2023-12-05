package datastructures

import constants.PEER_HANDSHAKE_LENGTH_BYTES
import datastructures.state.ReadyForDownload
import util.Encoders
import util.PieceDownloader
import java.io.DataInputStream
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.SocketFactory

class RemotePeer {
    private val peer: InetSocketAddress
    private val socket: Socket = SocketFactory.getDefault().createSocket()
    private lateinit var peerConnection: RemotePeerConnection

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
        peerConnection = RemotePeerConnection(socket)

        val outputStream = socket.getOutputStream()
        val inputStream = DataInputStream(socket.getInputStream())

        val message = handShakeMessageFor(metaInfo)

        outputStream.write(message)
        outputStream.flush()

        val response = ByteArray(PEER_HANDSHAKE_LENGTH_BYTES)
        inputStream.readFully(response)

        return Encoders.hexEncode(response.copyOfRange(48, response.size))
    }

    fun getReadyForDownload() {
        if (!this::peerConnection.isInitialized) {
            throw IllegalStateException("peer messages cannot be read because peer handshake has not been done")
        }

        while (peerConnection.state !is ReadyForDownload) {
            peerConnection.process()
        }
        println("Connection is now ready for download")
    }

    suspend fun downloadPiece(metaInfo: MetaInfo, pieceNumber: Int, outputFile: String) {
        val downloader = PieceDownloader(metaInfo, peerConnection)
        val piece = downloader.download(pieceNumber)
        val file = File(outputFile)
        file.writeBytes(piece)
    }

    fun close() {
        if (!socket.isClosed) {
            socket.close()
        }
    }

    override fun toString(): String {
        return "${peer.hostString}:${peer.port}"
    }

    private fun handShakeMessageFor(metaInfo: MetaInfo): ByteArray {
        return byteArrayOf(19) +
                "BitTorrent protocol".toByteArray() +
                ByteArray(8) +
                metaInfo.infoHashBytes() +
                "00112233445566778899".toByteArray()
    }
}