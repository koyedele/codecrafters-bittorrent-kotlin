package commands

import datastructures.MetaInfo
import datastructures.PeersManager
import kotlinx.coroutines.runBlocking

class DownloadPieceCommand(
    private val outputFilePath: String,
    private val metaInfoFilePath: String,
    private val pieceNumber: Int
) : Command {
    override fun run() {
        val metaInfo = MetaInfo.fromFile(metaInfoFilePath)
        val peersManager = PeersManager(metaInfo)
        val remotePeers = peersManager.remotePeers()
        val remotePeer = remotePeers.random()
        val peerId = remotePeer.handShake(metaInfo)
        println(peerId)
        remotePeer.getReadyForDownload()
        runBlocking {
            remotePeer.downloadPiece(metaInfo, pieceNumber, outputFilePath)
        }
    }
}
