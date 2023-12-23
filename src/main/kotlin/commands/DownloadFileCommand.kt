package commands

import datastructures.MetaInfo
import domain.DownloadManager

class DownloadFileCommand(
    private val outputFilePath: String,
    private val metaInfoFilePath: String,
) : Command {
    override fun run() {
        val metaInfo = MetaInfo.fromFile(metaInfoFilePath)
        DownloadManager(metaInfo, outputFilePath).downloadFile()
        val fileName = outputFilePath.split("/").last().split(".").first()
        println("Downloaded $fileName.torrent to $outputFilePath.")
    }
}