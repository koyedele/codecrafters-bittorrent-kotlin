package datastructures.state

import datastructures.RemotePeerConnection

class RequestState : RemotePeerConnectionState {
    override fun next(connection: RemotePeerConnection) {
        connection.state = NullState()
    }

    override fun process(connection: RemotePeerConnection) {
        TODO("Not yet implemented")
    }
}
