package datastructures.state

import datastructures.PeerMessageType
import datastructures.RemotePeerConnection
import util.NetworkUtils.waitFor

internal class BitfieldState(override val connection: RemotePeerConnection) : RemotePeerConnectionState {
    override fun next() {
        connection.state = InterestedState(connection)
    }

    override fun process() {
        val message = waitFor(PeerMessageType.BITFIELD, connection.inputStream)

        println(message)
    }
}