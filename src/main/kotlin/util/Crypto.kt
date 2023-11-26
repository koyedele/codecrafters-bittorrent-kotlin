package util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Crypto {
     fun sha1Hash(data: ByteArray): ByteArray {
        return try {
            val md = MessageDigest.getInstance("SHA-1")
            md.digest(data)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}