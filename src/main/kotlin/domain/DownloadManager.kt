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

    private val peers: List<RemotePeer> by lazy { peersManager.remotePeers() }
    private lateinit var peersQueue: Channel<RemotePeer>

    suspend fun downloadFile() {
        peersQueue = Channel(peers.size)
        doDownload()
        workQueue.close()
        assembleOutputFile()
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
                    peer.downloadPiece(piece, "$outputFilePath-$piece")
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
            val file = File("$outputFilePath-$it")
            output.writeBytes(file.readBytes())
        }
    }
}