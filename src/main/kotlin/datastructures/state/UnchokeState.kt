package datastructures.state

import datastructures.PeerMessageType
import datastructures.RemotePeerConnection

class UnchokeState : RemotePeerConnectionState {
    override fun next(connection: RemotePeerConnection) {
        connection.state = NullState()
    }

    override fun process(connection: RemotePeerConnection) {
        val message = connection.waitFor(PeerMessageType.UNCHOKE)

        println(message)
    }
}
