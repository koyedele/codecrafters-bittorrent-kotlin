package commands

sealed interface Command {
    fun run(): Unit
}