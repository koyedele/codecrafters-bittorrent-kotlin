package commands

import datastructures.MetaInfo
import domain.PeersManager

class DownloadPieceCommand(
    private val outputFilePath: String,
    private val metaInfoFilePath: String,
    private val pieceNumber: Int
) : Command {
    override fun run() {
        val metaInfo = MetaInfo.fromFile(metaInfoFilePath)
        val peersManager = PeersManager(metaInfo)

        val remotePeer = peersManager.randomRemotePeer()

        remotePeer.downloadPiece(pieceNumber, outputFilePath)
        println("Piece $pieceNumber downloaded to $outputFilePath.")
    }
}
