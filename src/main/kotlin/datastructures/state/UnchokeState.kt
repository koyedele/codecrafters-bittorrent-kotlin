package datastructures.state

import datastructures.PeerMessageType
import datastructures.RemotePeerConnection
import util.NetworkUtils.waitFor

internal class UnchokeState(override val connection: RemotePeerConnection) : RemotePeerConnectionState {
    override fun next() {
        connection.state = ReadyForDownload(connection)
    }

    override fun process() {
        val message = waitFor(PeerMessageType.UNCHOKE, connection.inputStream)

        println(message)
    }
}
