import com.google.gson.Gson;
import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type

val gson = Gson()
const val IntegerStartTag = 'i'

fun main(args: Array<String>) {
    val command = args[0]
    when (command) {
        "decode" -> {
            val bencodedValue = args[1]
            val decoded: Any = when {
                bencodedValue.startsWith("i") -> Bencode().decode(bencodedValue.toByteArray(), Type.NUMBER)
                bencodedValue.startsWith("l") -> Bencode().decode(bencodedValue.toByteArray(), Type.LIST)
                bencodedValue.startsWith("d") -> Bencode().decode(bencodedValue.toByteArray(), Type.DICTIONARY)
                else -> Bencode().decode(bencodedValue.toByteArray(), Type.STRING)
            }
             println(gson.toJson(decoded))
             return
        }
        else -> println("Unknown command $command")
    }
}