package domain

import constants.NUM_BYTES_OF_EACH_PEER_IN_LIST
import datastructures.DictValue
import datastructures.MetaInfo
import util.Encoders
import util.toPort
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer

class PeersManager(private val metaInfo: MetaInfo) {
    private val peers: List<RemotePeer> by lazy { retrieveRemotePeers() }

    fun getPeerAddresses(): List<String> = peers.map { it.toString() }

    fun remotePeers(): List<RemotePeer> = peers.map { it }

    fun randomRemotePeer(): RemotePeer = remotePeers().random()

    private fun getParamsString(): String {
        val params: Map<String, String> = mapOf(
            "info_hash" to Encoders.urlEncode(metaInfo.infoHashBytes()),
            "peer_id" to Encoders.urlEncode("00112233445566778899"),
            "port" to Encoders.urlEncode("6881"),
            "uploaded" to Encoders.urlEncode("0"),
            "downloaded" to Encoders.urlEncode("0"),
            "left" to Encoders.urlEncode(metaInfo.length().toString()),
            "compact" to Encoders.urlEncode("1"),
        )

        return params.entries.joinToString("&") { (key, value) -> "$key=$value" }
    }

    private fun retrieveRemotePeers(): List<RemotePeer> {
        println("Retrieving remote peers...")
        val params = getParamsString()
        val url = URL("${metaInfo.trackerUrl()}?$params")
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.setRequestMethod("GET")

        val encodedBytes = con.inputStream.use { it.readBytes() }

        val dict = DictValue.of(encodedBytes)
        val encodedPeers = (dict["peers"] as ByteBuffer).array()

        return encodedPeers.asIterable()
            .chunked(NUM_BYTES_OF_EACH_PEER_IN_LIST)
            .map { remotePeerFrom(it.toByteArray()) }
    }

    private fun remotePeerFrom(data: ByteArray): RemotePeer {
        val addr = data.copyOf(4)
        val portData = data.copyOfRange(4, data.size)
        val port = portData.toPort()

        val socket = InetSocketAddress(
            InetAddress.getByAddress(addr).hostAddress,
            port
        )

        return RemotePeer(socket, metaInfo)
    }
}