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
        return "PeerMessage(messageLength=$messageLength, messageType=$messageType, payload=${payload.contentToString()})"
    }

    companion object {
        fun requestMessageTypeFor(index: Int, blockSize: Int): PeerMessage {
            val payload = ByteBuffer
                .allocate(12)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(index)
                .putInt(index * PIECE_DOWNLOAD_SIZE_BYTES)
                .putInt(blockSize)

            payload.rewind()
            val data = ByteArray(payload.remaining())
            payload.get(data)

            return PeerMessage(PeerMessageType.REQUEST, data)
        }
    }
}