import com.google.gson.Gson;
import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import java.io.File
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

val gson = Gson()
const val INTEGER_TAG = "i"
const val LIST_TAG = "l"
const val DICT_TAG = "d"
const val NUM_BYTES_IN_PIECE_HASH = 20

fun main(args: Array<String>) {
    when (val command = args[0]) {
        "decode" -> runDecodeCommand(args[1])
        "info" -> runInfoCommand(args[1])
        else -> println("Unknown command $command")
    }
}

sealed interface Value {
    fun toJson(): String
}

class StringValue(private val value: String) : Value {
    override fun toJson(): String = gson.toJson(value)
}

class IntValue(private val value: Long) : Value {
    override fun toJson(): String = gson.toJson(value)
    fun asInt(): Int = value.toInt()
}

class ListValue(private val value: List<Any>) : Value {
    override fun toJson(): String = gson.toJson(value)
}

class DictValue(private val value: Map<String, Any>) : Value {
    operator fun get(key: String) = value[key]

    override fun toJson(): String = gson.toJson(value)
}

@Suppress("UNCHECKED_CAST")
class MetaInfo(private val value: DictValue) : Value {
    private val info: Map<String, Any> = value["info"] as Map<String, Any>

    fun trackerUrl(): String =
        (value["announce"] as ByteBuffer).array().toString(Charset.defaultCharset())

    fun length(): Int = (info["length"] as Long).toInt()

    fun name(): String = (info["name"] as ByteBuffer).array().toString(Charset.defaultCharset())

    fun pieceLength(): Int = (info["piece length"] as Long).toInt()

    fun infoHash(): String {
        val encoded = Bencode().encode(info)
        return sha1Hash(encoded)
    }

    fun pieceHashes(): List<String> {
        return pieces()
            .asIterable()
            .chunked(NUM_BYTES_IN_PIECE_HASH)
            .map { it.joinToString("") { str -> "%02x".format(str) } }
    }

    private fun pieces(): ByteArray = (info["pieces"] as ByteBuffer).array()

    private fun sha1Hash(data: ByteArray): String {
        return try {
            val md = MessageDigest.getInstance("SHA-1")
            val digest = md.digest(data)
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    override fun toString(): String = toJson()

    override fun toJson(): String = value.toJson()
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

private fun runDecodeCommand(input: String) {
    val decoded = parseValue(input)
    println(decoded.toJson())
}

private fun runInfoCommand(filePath: String) {
    val contents = File(filePath).readBytes()
    val metaInfo = parseMetaInfo(contents)
    println("Tracker URL: ${metaInfo.trackerUrl()}")
    println("Length: ${metaInfo.length()}")
    println("Info Hash: ${metaInfo.infoHash()}")
    println("Piece Length: ${metaInfo.pieceLength()}")
    println("Piece Hashes:")
    println(metaInfo.pieceHashes().joinToString("\n"))

}