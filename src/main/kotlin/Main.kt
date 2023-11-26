import commands.DecodeCommand
import commands.HandshakeCommand
import commands.InfoCommand
import commands.PeersCommand

fun main(args: Array<String>) {
    if (args.size < 2) {
        return help()
    }

    try {
        when (val command = args[0]) {
            "decode" -> DecodeCommand(args[1]).run()
            "info" -> InfoCommand(args[1]).run()
            "peers" -> PeersCommand(args[1]).run()
            "handshake" -> HandshakeCommand(args[1], args[2]).run()
            else -> println("Unknown command $command")
        }
    } catch (e: Exception) {
        println(e.message)
        help()
    }
}

private fun help() {
    print("Usage: ./your_bittorrent.sh <command-and-args>\n")
    print("    <command-and-args> can be:\n")
    print("\n")
    print("    decode <input-string>                                -- decode a Bencoded <input-string>\n")
    print("    info <torrent-file-path>                             -- print info about a Torrent file\n")
    print("    peers <torrent-file-path>                            -- discover peers using information in\n")
    print("                                                            the Torrent file and print their details\n")
    print("    handshake <torrent-file-path> <peer_ip>:<peer_port>  -- establish a TCP connection with the peer\n")
    print("                                                            at the given address and complete a peer\n")
    print("                                                            handshake\n")
    println()
}
