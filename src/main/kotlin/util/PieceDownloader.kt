package util

import constants.PIECE_DOWNLOAD_SIZE_BYTES
import datastructures.MetaInfo
import datastructures.PeerMessage
import datastructures.PeerMessageType
import datastructures.RemotePeerConnection
import kotlinx.coroutines.*
import kotlin.math.ceil

class PieceDownloader(
    private val metaInfo: MetaInfo,
    private val remotePeerConnection: RemotePeerConnection
) {
    private val totalFileLength = metaInfo.length()
    private val singlePieceLength = metaInfo.pieceLength()
    private val numPieces = ceil(totalFileLength / singlePieceLength.toDouble()).toInt()
    private val lastPieceLength = totalFileLength % singlePieceLength

    suspend fun download(pieceNumber: Int): ByteArray {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun downloadPiece(numRequests: Int): ByteArray = coroutineScope {
        val messages = (1..numRequests)
            .map {
                async(Dispatchers.Default.limitedParallelism(5)) {
                    remotePeerConnection.waitFor(PeerMessageType.PIECE)
                }
            }.awaitAll()

        messages
            .sortedBy { it.payload.copyOfRange(0, 4).toInt() }
            .fold(ByteArray(0)) { data, message ->

                data + message.payload.copyOfRange(8, message.payload.size)
            }
    }

    private fun sendRequestMessagesFor(pieceNumber: Int): Int {
        val pieceLength = if (isLastPiece(pieceNumber)) lastPieceLength else singlePieceLength
        val numBlocks = ceil(pieceLength / PIECE_DOWNLOAD_SIZE_BYTES.toDouble()).toInt()
        var lastBlockSize = pieceLength % PIECE_DOWNLOAD_SIZE_BYTES

        if (lastBlockSize == 0) {
            lastBlockSize = PIECE_DOWNLOAD_SIZE_BYTES
        }

        for (index in 0..<numBlocks) {
            val size = if (index == numBlocks - 1) lastBlockSize else PIECE_DOWNLOAD_SIZE_BYTES
            val peerMessage = PeerMessage.requestMessageTypeFor(index, size)
            remotePeerConnection.sendMessage(peerMessage)
        }

        return numBlocks
    }

    private fun isLastPiece(pieceNumber: Int) = pieceNumber == numPieces - 1

    class InvalidPieceException(message: String) : Exception(message)
}