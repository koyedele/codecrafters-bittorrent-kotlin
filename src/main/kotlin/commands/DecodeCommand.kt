package commands

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import datastructures.DictValue
import datastructures.IntValue
import datastructures.ListValue
import datastructures.StringValue
import constants.DICT_TAG
import constants.INTEGER_TAG
import constants.LIST_TAG

class DecodeCommand(private val input: String) : Command {
    override fun run() {
        val decoded = when {
            input[0] in '1'..'9' -> StringValue(decode(input.toByteArray(), Type.STRING))
            input.startsWith(INTEGER_TAG) -> IntValue(decode(input.toByteArray(), Type.NUMBER))
            input.startsWith(LIST_TAG) -> ListValue(decode(input.toByteArray(), Type.LIST))
            input.startsWith(DICT_TAG) -> DictValue(decode(input.toByteArray(), Type.DICTIONARY))
            else -> throw IllegalArgumentException("Unknown input: $input")
        }
        println(decoded.toJson())
    }

    private fun <T> decode(input: ByteArray, type: Type<T>): T = Bencode().decode(input, type)
}