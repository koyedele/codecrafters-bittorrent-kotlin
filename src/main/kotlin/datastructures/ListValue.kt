package datastructures

class ListValue(private val value: List<Any>) : Value {
    override fun toJson(): String = gson.toJson(value)
}