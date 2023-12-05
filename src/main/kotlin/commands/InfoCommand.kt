package commands

import datastructures.MetaInfo
import util.Encoders

class InfoCommand(private val filePath: String) : Command {
    override fun run() {
        val metaInfo = MetaInfo.fromFile(filePath)
        val pieceHashes = Encoders.hexEncode(metaInfo.piecesBytes())
        val infoHash = Encoders.hexEncode(metaInfo.infoHashBytes())

        println("Tracker URL: ${metaInfo.trackerUrl()}")
        println("Length: ${metaInfo.length()}")
        println("Info Hash: $infoHash")
        println("Piece Length: ${metaInfo.pieceLength()}")
        println("Piece Hashes:")
        println(pieceHashes)
    }
}