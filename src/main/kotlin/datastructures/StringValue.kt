package datastructures

class StringValue(private val value: String) : Value {
    override fun toJson(): String = gson.toJson(value)
}
