package util

import org.apache.commons.codec.binary.StringUtils
import org.apache.commons.codec.net.URLCodec

object Encoders {
    fun hexEncode(data: ByteArray): String = data.joinToString("") { "%02x".format(it) }

    fun hexEncodeInChunks(pieces: ByteArray, chunkSize: Int): String {
        return pieces
            .asIterable()
            .chunked(chunkSize)
            .joinToString("\n") { it.joinToString("") { str -> "%02x".format(str) } }
    }

    fun urlEncode(data: String): String = urlEncode(StringUtils.getBytesUtf8(data))
    fun urlEncode(data: ByteArray): String = StringUtils.newStringUtf8(URLCodec.encodeUrl(null, data))
}