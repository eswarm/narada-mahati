package `in`.eswarm.mahati.db // Or your preferred package

enum class MessageDirection(val code: Long) {
    RECEIVED(0L),
    SENT(1L);

    companion object {
        fun fromCode(code: Long): MessageDirection? = entries.find { it.code == code }
    }
}


