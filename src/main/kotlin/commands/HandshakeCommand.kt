package commands

import datastructures.MetaInfo
import datastructures.RemotePeer

class HandshakeCommand(
    private val filePath: String,
    peerAddress: String
) : Command {
    private val remotePeer = RemotePeer(peerAddress)

    override fun run() {
        val metaInfo = MetaInfo.fromFile(filePath)

        val peerId = remotePeer.handShake(metaInfo)
        println("Peer ID: $peerId")
        remotePeer.close()
    }
}