package commands

import datastructures.MetaInfo
import util.Encoders.hexEncode
import util.Encoders.hexEncodeInChunks
import constants.NUM_BYTES_OF_EACH_PIECE_IN_PIECE_HASH
import java.io.File

class InfoCommand(private val filePath: String) : Command {
    override fun run() {
        val contents = File(filePath).readBytes()
        val metaInfo = MetaInfo.parseMetaInfo(contents)
        val pieceHashes = hexEncodeInChunks(metaInfo.piecesBytes(), NUM_BYTES_OF_EACH_PIECE_IN_PIECE_HASH)
        val infoHash = hexEncode(metaInfo.infoHashBytes())

        println("Tracker URL: ${metaInfo.trackerUrl()}")
        println("Length: ${metaInfo.length()}")
        println("Info Hash: $infoHash")
        println("Piece Length: ${metaInfo.pieceLength()}")
        println("Piece Hashes:")
        println(pieceHashes)
    }
}