package datastructures

import constants.PEER_MESSAGE_ID_LENGTH_BYTES
import constants.PEER_MESSAGE_LENGTH_PREFIX_BYTES
import datastructures.state.BitfieldState
import datastructures.state.RemotePeerConnectionState
import util.toInt
import java.io.DataInputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RemotePeerConnection(socket: Socket) {
    var state: RemotePeerConnectionState = BitfieldState()
    private val inputStream: DataInputStream = DataInputStream(socket.getInputStream())
    private val outputStream: OutputStream = socket.getOutputStream()

    fun process() {
        state.process(this)
        state.next(this)
    }

    fun waitFor(messageType: PeerMessageType): PeerMessage {
        var message = readMessage()
        println("Got message: $message, expecting $messageType")
        while (message.messageType != messageType) {
            message = readMessage()
            println("Got message: $message, expecting $messageType")
        }

        return message
    }

    private fun readMessage(): PeerMessage {
        val data = ByteArray(PEER_MESSAGE_LENGTH_PREFIX_BYTES)
        inputStream.readFully(data)

        val messageLength = data.toInt()
        if (messageLength == 0) {
            return PeerMessage(messageLength, PeerMessageType.KEEP_ALIVE, ByteArray(0))
        }

        val messageType = inputStream.readByte().toInt()
        val type = PeerMessageType.valueOf(messageType)

        val payload = if (messageLength > 1) {
            val remaining = ByteArray(messageLength - 1)
            inputStream.readFully(remaining)
            remaining
        } else {
            ByteArray(0)
        }

        return PeerMessage(messageLength, type, payload)
    }

    fun sendMessage(message: PeerMessage) {
        val length = PEER_MESSAGE_LENGTH_PREFIX_BYTES + PEER_MESSAGE_ID_LENGTH_BYTES + message.payload.size
        val buffer = ByteBuffer
            .allocate(length)
            .order(ByteOrder.BIG_ENDIAN)
            .putInt(length - PEER_MESSAGE_LENGTH_PREFIX_BYTES)
            .put(message.messageType.messageId.toByte())
            .put(message.payload)

        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        println("Sending payload: ${data.contentToString()}")
        outputStream.write(data)
    }
}
