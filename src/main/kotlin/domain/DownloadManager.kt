package domain

import datastructures.MetaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
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
    private val pieceData = ConcurrentHashMap<Int, ByteArray>()

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
            println("Total number of pieces: $numPieces")
            repeat(numPieces) { workQueue.send(it) }
            repeat(peers.size) { peersQueue.send(peers[it]) }
            repeat(numPieces) { runDownloader(peersQueue, workQueue) }
        }
    }


    private suspend fun runDownloader(peers: Channel<RemotePeer>, pieces: Channel<Int>) = coroutineScope {
        launch {
            if (numDownloadedPieces.get() != numPieces) {
                val piece = pieces.receive()
                val peer = peers.receive()
                println("Started downloading piece #$piece from peer $peer")
                try {
                    val bytes = peer.downloadPieceBytes(piece)
                    pieceData[piece] = bytes
                } catch (e: Exception) {
                    println("Error while downloading piece #$piece")
                    println(e)
                    println("Re-enqueueing piece $piece")
                    pieces.send(piece)
                }

                numDownloadedPieces.incrementAndGet()
                peers.send(peer)
            }
        }
    }

    private fun assembleOutputFile() {
        val output = File(outputFilePath)
        val fullData = pieceData.keys
            .sorted()
            .fold(ByteArray(0)) { acc, index -> acc + pieceData[index]!! }

        output.writeBytes(fullData)
    }

    private fun cleanupResources() {
        workQueue.close()
        peersQueue.close()
    }
}