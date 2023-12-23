package commands

import datastructures.MetaInfo
import domain.PeersManager

class PeersCommand(private val filePath: String) : Command {
    override fun run() {
        val metaInfo = MetaInfo.fromFile(filePath)
        val addresses = PeersManager(metaInfo).getPeerAddresses().joinToString("\n")

        println(addresses)
    }
}