package datastructures

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
}