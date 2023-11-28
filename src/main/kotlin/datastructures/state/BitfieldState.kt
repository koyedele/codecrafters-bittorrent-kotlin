package datastructures.state

import datastructures.PeerMessageType
import datastructures.RemotePeerConnection

class BitfieldState : RemotePeerConnectionState {
    override fun next(connection: RemotePeerConnection) {
        connection.state = InterestedState()
    }

    override fun process(connection: RemotePeerConnection) {
        val message = connection.waitFor(PeerMessageType.BITFIELD)

        println(message)
    }
}