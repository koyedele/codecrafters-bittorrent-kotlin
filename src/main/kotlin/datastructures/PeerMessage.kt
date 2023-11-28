package datastructures

class PeerMessage(val messageType: PeerMessageType, var payload: ByteArray) {
    private var messageLength: Int = 0

    constructor(messageLength: Int, messageType: PeerMessageType, payload: ByteArray): this(messageType, payload) {
        this.messageLength = messageLength
    }

    override fun toString(): String {
        return "PeerMessage(messageLength=$messageLength, messageType=$messageType, payload=${payload.contentToString()})"
    }
}