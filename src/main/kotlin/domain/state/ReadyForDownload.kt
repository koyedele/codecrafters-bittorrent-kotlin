package domain.state

import domain.RemotePeerConnection

internal class ReadyForDownload(override val connection: RemotePeerConnection) : RemotePeerConnectionState {
    override fun next() {}

    override fun process() {}
}