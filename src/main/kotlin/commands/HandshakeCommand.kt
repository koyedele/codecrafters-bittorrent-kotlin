package commands

import datastructures.MetaInfo
import domain.RemotePeer

class HandshakeCommand(
    private val filePath: String,
    private val peerAddress: String
) : Command {
    override fun run() {
        val metaInfo = MetaInfo.fromFile(filePath)
        val remotePeer = RemotePeer(peerAddress, metaInfo)

        val peerId = remotePeer.handShake()
        println("Peer ID: $peerId")
        remotePeer.close()
    }
}