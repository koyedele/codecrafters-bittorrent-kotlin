package domain

import datastructures.MetaInfo

class DownloadManager(
    private val metaInfo: MetaInfo,
    private val outputFilePath: String,
) {
    fun downloadFile(): Unit = TODO()
}