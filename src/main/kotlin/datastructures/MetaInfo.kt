package datastructures

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import constants.NUM_BYTES_OF_EACH_PIECE_IN_PIECE_HASH
import util.Crypto.sha1Hash
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset

@Suppress("UNCHECKED_CAST")
class MetaInfo(private val value: DictValue) : Value {
    private val info: Map<String, Any> = value["info"] as Map<String, Any>

    fun trackerUrl(): String =
        (value["announce"] as ByteBuffer).array().toString(Charset.defaultCharset())

    fun length(): Int = (info["length"] as Long).toInt()

    fun name(): String = (info["name"] as ByteBuffer).array().toString(Charset.defaultCharset())

    fun pieceLength(): Int = (info["piece length"] as Long).toInt()

    fun piecesBytes(): List<ByteArray> = (info["pieces"] as ByteBuffer)
        .array()
        .asIterable()
        .chunked(NUM_BYTES_OF_EACH_PIECE_IN_PIECE_HASH)
        .map { it.toByteArray() }

    fun infoHashBytes(): ByteArray = sha1Hash(infoBytes())

    private fun infoBytes(): ByteArray = Bencode().encode(info)

    override fun toString(): String = toJson()

    override fun toJson(): String = value.toJson()

    companion object {
        fun fromFile(filePath: String): MetaInfo {
            val contents = File(filePath).readBytes()
            val decoded = DictValue(Bencode(true).decode(contents, Type.DICTIONARY))
            return MetaInfo(decoded)
        }
    }
}