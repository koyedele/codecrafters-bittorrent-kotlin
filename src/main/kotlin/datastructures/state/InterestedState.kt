package datastructures.state

import datastructures.PeerMessage
import datastructures.PeerMessageType
import datastructures.RemotePeerConnection
import util.NetworkUtils.sendMessage

internal class InterestedState(override val connection: RemotePeerConnection) : RemotePeerConnectionState {
    override fun next() {
        connection.state = UnchokeState(connection)
    }

    override fun process() {
        val message = PeerMessage(PeerMessageType.INTERESTED, ByteArray(0))
        println("Sending INTERESTED message to peer: $message}")
        sendMessage(message, connection.outputStream)
    }
}
