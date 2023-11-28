package datastructures.state

import datastructures.PeerMessage
import datastructures.PeerMessageType
import datastructures.RemotePeerConnection

class InterestedState : RemotePeerConnectionState {
    override fun next(connection: RemotePeerConnection) {
        connection.state = UnchokeState()
    }

    override fun process(connection: RemotePeerConnection) {
        val message = PeerMessage(PeerMessageType.INTERESTED, ByteArray(0))
        println("Sending INTERESTED message to peer: $message}")
        connection.sendMessage(message)
    }
}
