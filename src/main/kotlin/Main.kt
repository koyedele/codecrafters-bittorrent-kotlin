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

fun main(args: Array<String>) {
    val command = args[0]
    when (command) {
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
            .chunked(20)
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

private fun parse(encodedValue: String): Value {
    return when {
        encodedValue[0] in '1'..'9' -> StringValue(decode(encodedValue.toByteArray(), Type.STRING))
        encodedValue.startsWith("i") -> IntValue(decode(encodedValue.toByteArray(), Type.NUMBER))
        encodedValue.startsWith("l") -> ListValue(decode(encodedValue.toByteArray(), Type.LIST))
        encodedValue.startsWith("d") -> DictValue(decode(encodedValue.toByteArray(), Type.DICTIONARY))
        else -> throw IllegalArgumentException("Unknown input: $encodedValue")
    }
}

private fun runDecodeCommand(input: String) {
    val decoded = parse(input)
    println(decoded.toJson())
}

private fun runInfoCommand(filePath: String) {
    val contents = File(filePath).readBytes()
    val decoded = DictValue(Bencode(true).decode(contents, Type.DICTIONARY))
    val metaInfo = MetaInfo(decoded)
    println("Tracker URL: ${metaInfo.trackerUrl()}")
    println("Length: ${metaInfo.length()}")
    println("Info Hash: ${metaInfo.infoHash()}")
    println("Piece Length: ${metaInfo.pieceLength()}")
    println("Piece Hashes:")
    println(metaInfo.pieceHashes().joinToString("\n"))

}