package domain

import domain.state.BitfieldState
import domain.state.RemotePeerConnectionState
import java.io.DataInputStream
import java.io.OutputStream
import java.net.Socket

class RemotePeerConnection(socket: Socket) {
    val inputStream: DataInputStream = DataInputStream(socket.getInputStream())
    val outputStream: OutputStream = socket.getOutputStream()
    var state: RemotePeerConnectionState = BitfieldState(this)

    fun processState() {
        state.process()
        state.next()
    }
}
