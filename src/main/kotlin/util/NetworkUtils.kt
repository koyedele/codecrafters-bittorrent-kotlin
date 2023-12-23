package util

import constants.PEER_MESSAGE_ID_LENGTH_BYTES
import constants.PEER_MESSAGE_LENGTH_PREFIX_BYTES
import datastructures.PeerMessage
import datastructures.PeerMessageType
import mu.KotlinLogging
import java.io.DataInputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object NetworkUtils {
    private val logger = KotlinLogging.logger {}
    
    fun waitFor(messageType: PeerMessageType, inputStream: DataInputStream): PeerMessage {
        var message = readMessage(inputStream)
        logger.info { "Got message: $message, expecting $messageType" }
        while (message.messageType != messageType) {
            message = readMessage(inputStream)
            logger.info { "Got message: $message, expecting $messageType" }
        }

        return message
    }

    fun sendMessage(message: PeerMessage, outputStream: OutputStream) {
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
        logger.info { "Sending payload: ${data.contentToString()}" }
        outputStream.write(data)
    }

    private fun readMessage(inputStream: DataInputStream): PeerMessage {
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
}