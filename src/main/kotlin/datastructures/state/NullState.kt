package datastructures.state

import datastructures.RemotePeerConnection

class NullState : RemotePeerConnectionState {
    override fun next(connection: RemotePeerConnection) {}

    override fun process(connection: RemotePeerConnection) {}
}