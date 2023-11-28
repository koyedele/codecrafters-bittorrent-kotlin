package datastructures.state

import datastructures.RemotePeerConnection

interface RemotePeerConnectionState {
    fun next(connection: RemotePeerConnection)
    fun process(connection: RemotePeerConnection)
}
