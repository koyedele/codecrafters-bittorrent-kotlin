package commands

import constants.NUM_BYTES_OF_EACH_PEER_IN_LIST
import datastructures.DictValue
import datastructures.MetaInfo
import util.Encoders.urlEncode
import java.io.File
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PeersCommand(private val filePath: String) : Command {
    override fun run() {
        val contents = File(filePath).readBytes()
        val metaInfo = MetaInfo.parseMetaInfo(contents)
        val addresses = getPeerAddresses(metaInfo).joinToString("\n")

        println(addresses)
    }

    private fun getParamsString(metaInfo: MetaInfo): String {
        val params: Map<String, String> = mapOf(
            "info_hash" to urlEncode(metaInfo.infoHashBytes()),
            "peer_id" to urlEncode("00112233445566778899"),
            "port" to urlEncode("6881"),
            "uploaded" to urlEncode("0"),
            "downloaded" to urlEncode("0"),
            "left" to urlEncode(metaInfo.length().toString()),
            "compact" to urlEncode("1"),
        )

        return params.entries.joinToString("&") { (key, value) -> "$key=$value" }
    }

    private fun getPeerAddressString(data: ByteArray): String {
        val addr = data.copyOf(4)
        val portData = data.copyOfRange(4, data.size)
        val port = ByteBuffer.wrap(portData).order(ByteOrder.BIG_ENDIAN).getShort().toInt() and 0xFFFF

        val socket = InetSocketAddress(
            InetAddress.getByAddress(addr),
            port
        )
        return "${socket.hostString}:${socket.port}"
    }

    private fun getPeerAddresses(metaInfo: MetaInfo): List<String> {
        val params = getParamsString(metaInfo)
        val url = URL("${metaInfo.trackerUrl()}?$params")
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.setRequestMethod("GET")

        val encodedBytes = con.inputStream.readBytes()
        val dict = DictValue.of(encodedBytes)
        val peers = (dict["peers"] as ByteBuffer).array()

        return peers.asIterable()
            .chunked(NUM_BYTES_OF_EACH_PEER_IN_LIST)
            .map { getPeerAddressString(it.toByteArray()) }
    }
}