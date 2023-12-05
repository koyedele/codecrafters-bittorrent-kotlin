package util

import org.apache.commons.codec.binary.StringUtils
import org.apache.commons.codec.net.URLCodec

object Encoders {
    fun hexEncode(data: ByteArray): String = data.joinToString("") { "%02x".format(it) }
    fun hexEncode(pieces: List<ByteArray>): String =
        pieces.map { hexEncode(it) }.joinToString("\n")

    fun urlEncode(data: String): String = urlEncode(StringUtils.getBytesUtf8(data))
    fun urlEncode(data: ByteArray): String = StringUtils.newStringUtf8(URLCodec.encodeUrl(null, data))
}