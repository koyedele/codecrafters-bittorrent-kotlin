package datastructures

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import java.nio.ByteBuffer

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

    companion object {
        fun of(data: ByteArray): DictValue {
            return DictValue(Bencode(true).decode(data, Type.DICTIONARY))
        }
    }
}