package domain.state

import domain.RemotePeerConnection

interface RemotePeerConnectionState {
    val connection: RemotePeerConnection
    fun next()
    fun process()
}
