package datastructures

import com.google.gson.Gson

val gson = Gson()

sealed interface Value {
    fun toJson(): String
}