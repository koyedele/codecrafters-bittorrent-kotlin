package domain

import datastructures.MetaInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil

class DownloadManager(
    metaInfo: MetaInfo,
    private val outputFilePath: String,
) {
    private val peersManager: PeersManager = PeersManager(metaInfo)
    private val totalFileLength = metaInfo.length()
    private val singlePieceLength = metaInfo.pieceLength()
    private val numPieces = ceil(totalFileLength / singlePieceLength.toDouble()).toInt()
    private val workQueue = Channel<Int>(numPieces)
    private val numDownloadedPieces = AtomicInteger(0)
    private val logger = KotlinLogging.logger {}

    private lateinit var peers: List<RemotePeer>
    private lateinit var peersQueue: Channel<RemotePeer>

    suspend fun downloadFile() {
        peers = peersManager.remotePeers()
        peersQueue = Channel(peers.size)

        doDownload()
        assembleOutputFile()

        cleanupResources()
    }

    private suspend fun doDownload() = coroutineScope {
        withContext(Dispatchers.IO) {
            repeat(numPieces) { workQueue.send(it) }
            repeat(peers.size) { peersQueue.send(peers[it]) }
            repeat(peers.size) { runDownloader(peersQueue, workQueue) }
        }
    }


    private suspend fun runDownloader(peers: Channel<RemotePeer>, pieces: Channel<Int>) = coroutineScope {
        launch {
            if (numDownloadedPieces.get() != numPieces) {
                val piece = pieces.receive()
                val peer = peers.receive()
                logger.info { "Started downloading piece #$piece from peer $peer" }
                try {
                    peer.downloadPiece(piece, fileNameFor(piece))
                } catch (e: Exception) {
                    logger.info { "Error while downloading piece #$piece" }
                    logger.info { e }
                    logger.info { "Re-enqueueing piece $piece" }
                    pieces.send(piece)
                }

                numDownloadedPieces.incrementAndGet()
                peers.send(peer)
            }
        }
    }

    private fun assembleOutputFile() {
        val output = File(outputFilePath)
        repeat(numPieces) {
            val file = File(fileNameFor(it))
            output.writeBytes(file.readBytes())
        }
    }

    private fun fileNameFor(piece: Int) = "$outputFilePath-$piece"

    private fun cleanupResources() {
        workQueue.close()
        peersQueue.close()

        repeat(numPieces) {
            File(fileNameFor(it)).delete()
        }
    }
}