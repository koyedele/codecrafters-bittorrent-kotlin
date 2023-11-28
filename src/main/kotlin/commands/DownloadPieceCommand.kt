package commands

import datastructures.MetaInfo
import datastructures.PeersManager

class DownloadPieceCommand(
    private val outputFilePath: String,
    private val metaInfoFilePath: String,
    private val pieceNumber: Number
) : Command {
    override fun run() {
        val metaInfo = MetaInfo.fromFile(metaInfoFilePath)
        val peersManager = PeersManager(metaInfo)
        val remotePeers = peersManager.remotePeers()
        val remotePeer = remotePeers.random()
        val peerId = remotePeer.handShake(metaInfo)
        println(peerId)
        remotePeer.peerMessages()
    }
}
