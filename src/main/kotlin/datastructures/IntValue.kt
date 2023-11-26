package datastructures

class IntValue(private val value: Long) : Value {
    override fun toJson(): String = gson.toJson(value)
}
