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
             val decoded = if (bencodedValue.startsWith("i")) {
                 Bencode().decode(bencodedValue.toByteArray(), Type.NUMBER)
             } else {
                 Bencode().decode(bencodedValue.toByteArray(), Type.STRING)
             }
             println(gson.toJson(decoded))
             return
        }
        else -> println("Unknown command $command")
    }
}