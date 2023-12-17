package datastructures.state

import datastructures.RemotePeerConnection

interface RemotePeerConnectionState {
    val connection: RemotePeerConnection
    fun next()
    fun process()
}
