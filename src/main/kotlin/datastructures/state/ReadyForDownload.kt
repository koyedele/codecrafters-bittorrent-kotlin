package datastructures.state

import datastructures.RemotePeerConnection

class ReadyForDownload : RemotePeerConnectionState {
    override fun next(connection: RemotePeerConnection) {}

    override fun process(connection: RemotePeerConnection) {}
}