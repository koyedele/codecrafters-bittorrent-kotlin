package util

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.toPort(): Int = toShort().toInt() and 0xFFFF
fun ByteArray.toShort(): Short = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN).getShort()
fun ByteArray.toInt(): Int = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN).getInt()