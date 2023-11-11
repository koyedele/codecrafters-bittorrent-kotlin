import com.google.gson.Gson;
// import com.dampcake.bencode.Bencode; - available if you need it!

val gson = Gson()
const val IntegerStartTag = 'i'

fun main(args: Array<String>) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    // println("Logs from your program will appear here!")
    val command = args[0]
    when (command) {
        "decode" -> {
             // Uncomment this block to pass the first stage
             val bencodedValue = args[1]
             val decoded = decodeBencode(bencodedValue)
             println(gson.toJson(decoded))
             return
        }
        else -> println("Unknown command $command")
    }
}

fun decodeBencode(bencodedString: String): String {
    when {
        Character.isDigit(bencodedString[0]) -> {
            val firstColonIndex = bencodedString.indexOfFirst { it == ':' }
            val length = Integer.parseInt(bencodedString.substring(0, firstColonIndex))
            return bencodedString.substring(firstColonIndex + 1, firstColonIndex + 1 + length)
        }
        bencodedString[0] == IntegerStartTag -> {
            return bencodedString.substring(1, bencodedString.length - 1)
        }
        else -> TODO("Only strings are supported at the moment")
    }
}
