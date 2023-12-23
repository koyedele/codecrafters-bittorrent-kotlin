package domain

import constants.BITTORRENT_PROTOCOL_STRING
import constants.PEER_HANDSHAKE_LENGTH_BYTES
import constants.SELF_PEER_ID
import datastructures.MetaInfo
import domain.state.ReadyForDownload
import util.Encoders
import java.io.DataInputStream
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.SocketFactory

class RemotePeer {
    private val peer: InetSocketAddress
    private val socket: Socket = SocketFactory.getDefault().createSocket()
    private val metaInfo: MetaInfo
    private lateinit var peerConnection: RemotePeerConnection

    constructor(address: String, metaInfo: MetaInfo) {
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
        this.metaInfo = metaInfo
     }

    constructor(peerSocket: InetSocketAddress, metaInfo: MetaInfo) {
        peer = peerSocket
        this.metaInfo = metaInfo
    }

    fun handShake(): String {
        socket.connect(peer)
        peerConnection = RemotePeerConnection(socket)

        val message = handShakeMessage()

        with(socket.getOutputStream()) {
            write(message)
            flush()
        }

        val buffer = ByteArray(PEER_HANDSHAKE_LENGTH_BYTES)
        DataInputStream(socket.getInputStream()).readFully(buffer)

        return Encoders.hexEncode(buffer.copyOfRange(48, buffer.size))
    }

    fun downloadPiece(pieceNumber: Int, outputFile: String) {
        val piece = downloadPieceBytes(pieceNumber)
        val file = File(outputFile)
        file.writeBytes(piece)
    }

    fun downloadPieceBytes(pieceNumber: Int): ByteArray {
        getReadyForDownload()

        val downloader = PieceDownloader(metaInfo, peerConnection)
        val piece = downloader.download(pieceNumber)

        return piece
    }

    fun close() {
        if (!socket.isClosed) {
            socket.close()
        }
    }

    override fun toString(): String {
        return "${peer.hostString}:${peer.port}"
    }

    private fun getReadyForDownload() {
        if (!this::peerConnection.isInitialized) {
            handShake()
        }

        if (peerConnection.state is ReadyForDownload) {
            return
        }

        while (peerConnection.state !is ReadyForDownload) {
            peerConnection.processState()
        }
        println("Connection to $this is now ready for download")
    }

    private fun handShakeMessage(): ByteArray {
        return byteArrayOf(19) +
                BITTORRENT_PROTOCOL_STRING.toByteArray() +
                ByteArray(8) +
                metaInfo.infoHashBytes() +
                SELF_PEER_ID.toByteArray()
    }
}