package datastructures

import constants.PIECE_DOWNLOAD_SIZE_BYTES
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PeerMessage(val messageType: PeerMessageType, var payload: ByteArray) {
    private var messageLength: Int = 0

    constructor(messageLength: Int, messageType: PeerMessageType, payload: ByteArray): this(messageType, payload) {
        this.messageLength = messageLength
    }

    override fun toString(): String {
        val payloadContents = StringBuilder()

        if (payload.size > 16) {
            payloadContents.append(payload.slice(0..15)).append("...")
        } else {
            payloadContents.append(payload.contentToString())
        }

        return "PeerMessage(messageLength=$messageLength, messageType=$messageType, payload=$payloadContents)"
    }

    companion object {
        fun buildRequestMessageFor(pieceNumber: Int, index: Int, blockSize: Int): PeerMessage {
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
    }
}