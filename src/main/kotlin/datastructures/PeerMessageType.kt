package datastructures

enum class PeerMessageType(val messageId: Int) {
    KEEP_ALIVE(-1),
    CHOKE(0),
    UNCHOKE(1),
    INTERESTED(2),
    NOT_INTERESTED(3),
    HAVE(4),
    BITFIELD(5),
    REQUEST(6),
    PIECE(7),
    CANCEL(8),;

    companion object {
        fun valueOf(value: Int): PeerMessageType {
            return entries.first { it.messageId == value }
        }
    }
}
