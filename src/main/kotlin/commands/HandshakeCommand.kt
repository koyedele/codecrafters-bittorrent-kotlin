package commands

import datastructures.MetaInfo
import datastructures.RemotePeer
import java.io.File

class HandshakeCommand(
    private val filePath: String,
    peerAddress: String
) : Command {
    private val remotePeer: RemotePeer

    init {
        remotePeer = RemotePeer(peerAddress)
    }

    override fun run() {
        val contents = File(filePath).readBytes()
        val metaInfo = MetaInfo.parseMetaInfo(contents)

        val peerId = remotePeer.handShake(metaInfo)
        println("Peer ID: $peerId")
    }
}