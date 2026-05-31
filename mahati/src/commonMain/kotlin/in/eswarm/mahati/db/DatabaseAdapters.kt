package `in`.eswarm.mahati.db

import app.cash.sqldelight.ColumnAdapter

// Existing booleanAdapter
val booleanAdapter = object : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean = databaseValue != 0L
    override fun encode(value: Boolean): Long = if (value) 1L else 0L
}

// New adapter for MessageDirection
val messageDirectionAdapter = object : ColumnAdapter<MessageDirection, Long> {
    override fun decode(databaseValue: Long): MessageDirection {
        return MessageDirection.fromCode(databaseValue)
            ?: throw IllegalArgumentException("Unknown MessageDirection code: $databaseValue")
    }

    override fun encode(value: MessageDirection): Long {
        return value.code
    }
}
