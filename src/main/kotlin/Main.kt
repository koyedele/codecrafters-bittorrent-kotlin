import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import com.google.gson.Gson
import org.apache.commons.codec.binary.StringUtils
import org.apache.commons.codec.net.URLCodec
import java.io.File
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

val gson = Gson()
const val INTEGER_TAG = "i"
const val LIST_TAG = "l"
const val DICT_TAG = "d"
const val NUM_BYTES_OF_EACH_PIECE_IN_PIECE_HASH = 20
const val NUM_BYTES_OF_EACH_PEER_IN_LIST = 6

fun main(args: Array<String>) {
    if (args.size < 2) {
        return help()
    }

    when (val command = args[0]) {
        "decode" -> runDecodeCommand(args[1])
        "info" -> runInfoCommand(args[1])
        "peers" -> runPeersCommand(args[1])
        else -> println("Unknown command $command")
    }
}

private fun help() {
    print("Usage: ./your_bittorrent.sh <command-and-args>\n")
    print("         <command-and-args> can be:\n")
    print("\n")
    print("         decode <input-string>     -- decode a Bencoded <input-string>\n")
    print("         info <torrent-file-path>  -- print info about a Torrent file\n")
    print("         peers <torrent-file-path> -- discover peers using information in\n")
    print("                                      the Torrent file and print their details\n")
    println()
}

sealed interface Value {
    fun toJson(): String
}

class StringValue(private val value: String) : Value {
    override fun toJson(): String = gson.toJson(value)
}

class IntValue(private val value: Long) : Value {
    override fun toJson(): String = gson.toJson(value)
}

class ListValue(private val value: List<Any>) : Value {
    override fun toJson(): String = gson.toJson(value)
}

class DictValue(private val value: Map<String, Any>) : Value {
    operator fun get(key: String) = value[key]

    override fun toJson(): String {
        val decoded = value.keys.associateWith {
            if (value[it] is ByteBuffer) {
                (value[it] as ByteBuffer).array()
            } else {
                value[it]
            }
        }

        return gson.toJson(decoded)
    }
}

@Suppress("UNCHECKED_CAST")
class MetaInfo(private val value: DictValue) : Value {
    private val info: Map<String, Any> = value["info"] as Map<String, Any>

    fun trackerUrl(): String =
        (value["announce"] as ByteBuffer).array().toString(Charset.defaultCharset())

    fun length(): Int = (info["length"] as Long).toInt()

    fun name(): String = (info["name"] as ByteBuffer).array().toString(Charset.defaultCharset())

    fun pieceLength(): Int = (info["piece length"] as Long).toInt()

    fun piecesBytes(): ByteArray = (info["pieces"] as ByteBuffer).array()

    fun infoHashBytes(): ByteArray = sha1Hash(infoBytes())

    private fun infoBytes(): ByteArray = Bencode().encode(info)

    override fun toString(): String = toJson()

    override fun toJson(): String = value.toJson()
}

private fun sha1Hash(data: ByteArray): ByteArray {
    return try {
        val md = MessageDigest.getInstance("SHA-1")
        md.digest(data)
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException(e)
    }
}

private fun hexEncode(data: ByteArray): String = data.joinToString("") { "%02x".format(it) }

private fun urlEncode(data: String): String = urlEncode(StringUtils.getBytesUtf8(data))
private fun urlEncode(data: ByteArray): String = StringUtils.newStringUtf8(URLCodec.encodeUrl(null, data))

private fun hexEncodePieceHashes(pieces: ByteArray): String {
    return pieces
        .asIterable()
        .chunked(NUM_BYTES_OF_EACH_PIECE_IN_PIECE_HASH)
        .joinToString("\n") { it.joinToString("") { str -> "%02x".format(str) } }
}

private fun <T> decode(input: ByteArray, type: Type<T>): T = Bencode().decode(input, type)

private fun parseValue(encodedValue: String): Value {
    return when {
        encodedValue[0] in '1'..'9' -> StringValue(decode(encodedValue.toByteArray(), Type.STRING))
        encodedValue.startsWith(INTEGER_TAG) -> IntValue(decode(encodedValue.toByteArray(), Type.NUMBER))
        encodedValue.startsWith(LIST_TAG) -> ListValue(decode(encodedValue.toByteArray(), Type.LIST))
        encodedValue.startsWith(DICT_TAG) -> DictValue(decode(encodedValue.toByteArray(), Type.DICTIONARY))
        else -> throw IllegalArgumentException("Unknown input: $encodedValue")
    }
}

private fun parseMetaInfo(encodedBytes: ByteArray): MetaInfo {
    val decoded = DictValue(Bencode(true).decode(encodedBytes, Type.DICTIONARY))
    return MetaInfo(decoded)
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
    val dict = DictValue(Bencode(true).decode(encodedBytes, Type.DICTIONARY))
    val peers = (dict["peers"] as ByteBuffer).array()

    return peers.asIterable()
        .chunked(NUM_BYTES_OF_EACH_PEER_IN_LIST)
        .map{ getPeerAddressString(it.toByteArray()) }
}

private fun runDecodeCommand(input: String) {
    val decoded = parseValue(input)
    println(decoded.toJson())
}

private fun runInfoCommand(filePath: String) {
    val contents = File(filePath).readBytes()
    val metaInfo = parseMetaInfo(contents)
    val pieceHashes = hexEncodePieceHashes(metaInfo.piecesBytes())
    val infoHash = hexEncode(metaInfo.infoHashBytes())

    println("Tracker URL: ${metaInfo.trackerUrl()}")
    println("Length: ${metaInfo.length()}")
    println("Info Hash: $infoHash")
    println("Piece Length: ${metaInfo.pieceLength()}")
    println("Piece Hashes:")
    println(pieceHashes)
}

private fun runPeersCommand(filePath: String) {
    val contents = File(filePath).readBytes()
    val metaInfo = parseMetaInfo(contents)
    val addresses = getPeerAddresses(metaInfo).joinToString("\n")

    println(addresses)
}