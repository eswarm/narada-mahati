package `in`.eswarm.mahati.db

interface DbProvider {
    fun getDb(): MahatiDb
}

