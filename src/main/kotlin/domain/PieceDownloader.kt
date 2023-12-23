package domain

import constants.PIECE_DOWNLOAD_SIZE_BYTES
import datastructures.MetaInfo
import datastructures.PeerMessage
import datastructures.PeerMessageType
import util.Crypto
import util.Encoders
import util.NetworkUtils.sendMessage
import util.NetworkUtils.waitFor
import util.toInt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil

class PieceDownloader(
    private val metaInfo: MetaInfo,
    private val remotePeerConnection: RemotePeerConnection
) {
    private val totalFileLength = metaInfo.length()
    private val singlePieceLength = metaInfo.pieceLength()
    private val numPieces = ceil(totalFileLength / singlePieceLength.toDouble()).toInt()
    private val lastPieceLength = totalFileLength % singlePieceLength

    fun download(pieceNumber: Int): ByteArray {
        if (pieceNumber < 0 || pieceNumber >= numPieces) {
            throw IllegalArgumentException(
                "invalid piece number $pieceNumber. Expected between 0 and ${numPieces - 1}"
            )
        }

        val numRequests = sendRequestMessagesFor(pieceNumber)
        val piece = downloadPiece(numRequests)
        checkHash(pieceNumber, piece)

        return piece
    }

    private fun checkHash(pieceNumber: Int, piece: ByteArray) {
        val pieceHashFromMetaInfo = Encoders.hexEncode(metaInfo.piecesBytes()[pieceNumber])
        val pieceHashFromDownload = Encoders.hexEncode(Crypto.sha1Hash(piece))
        if (pieceHashFromMetaInfo != pieceHashFromDownload) {
            throw InvalidPieceException(
                "Piece download hash ($pieceHashFromDownload) did not match metaInfo hash ($pieceHashFromMetaInfo)"
            )
        }
    }

    private fun downloadPiece(numRequests: Int): ByteArray {
        val messages = (1..numRequests)
            .map {
                waitFor(PeerMessageType.PIECE, remotePeerConnection.inputStream)
            }

        return messages
            .sortedBy { it.payload.copyOfRange(0, 4).toInt() }
            .fold(ByteArray(0)) { data, message ->
                val file = message.payload.copyOfRange(8, message.payload.size)
                data + file
            }
    }

    private fun sendRequestMessagesFor(pieceIndex: Int): Int {
        val pieceLength = if (isLastPiece(pieceIndex)) lastPieceLength else singlePieceLength
        val numBlocks = ceil(pieceLength / PIECE_DOWNLOAD_SIZE_BYTES.toDouble()).toInt()
        var lastBlockSize = pieceLength % PIECE_DOWNLOAD_SIZE_BYTES

        if (lastBlockSize == 0) {
            lastBlockSize = PIECE_DOWNLOAD_SIZE_BYTES
        }

        for (index in 0..<numBlocks) {
            val blockLength = if (index == numBlocks - 1) lastBlockSize else PIECE_DOWNLOAD_SIZE_BYTES
            val peerMessage = buildRequestMessageFor(pieceIndex, index, blockLength)
            sendMessage(peerMessage, remotePeerConnection.outputStream)
        }

        return numBlocks
    }

    private fun isLastPiece(pieceNumber: Int) = pieceNumber == numPieces - 1

    private fun buildRequestMessageFor(pieceNumber: Int, index: Int, blockSize: Int): PeerMessage {
        val payload = ByteBuffer
            .allocate(12)
            .order(ByteOrder.BIG_ENDIAN)
            .putInt(pieceNumber)
            .putInt(index * PIECE_DOWNLOAD_SIZE_BYTES)
            .putInt(blockSize)

        payload.rewind()
        val data = ByteArray(payload.remaining())
        payload.get(data)

        return PeerMessage(PeerMessageType.REQUEST, data)
    }

    class InvalidPieceException(message: String) : Exception(message)
}