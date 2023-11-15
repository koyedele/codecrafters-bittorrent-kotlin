import com.google.gson.Gson;
import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import java.io.File

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
    override fun toJson(): String = gson.toJson(value)
}

@Suppress("UNCHECKED_CAST")
class MetaInfo(private val value: Map<String, Any>) : Value {
    private val info: Map<String, Any> = value["info"] as Map<String, Any>

    fun trackerUrl(): String = value["announce"] as String

    fun length(): Int = IntValue(info["length"] as Long).asInt()

    fun name(): String = info["name"] as String

    fun pieceLength(): Int = IntValue(info["piece length"] as Long).asInt()

    fun pieces(): String = info["pieces"] as String

    override fun toString(): String = toJson()

    override fun toJson(): String = gson.toJson(value)
}

private fun <T> decode(input: ByteArray, type: Type<T>): T = Bencode().decode(input, type)

private fun parse(encodedValue: String) : Value {
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
    val decoded = decode(contents, Type.DICTIONARY)
    val metaInfo = MetaInfo(decoded)
    println("Tracker URL: ${metaInfo.trackerUrl()}")
    println("Length: ${metaInfo.length()}")
}