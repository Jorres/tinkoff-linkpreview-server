package com.linkpreview.tarasov.processor

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class QueryDao(private val database: Database) {
    fun tryGetCachedQuery(queryUrl: String): Optional<String> {
        return transaction(database) {
            val res = CacheTable.select {CacheTable.url eq queryUrl}
            assert(res.count() <= 1)
            if (res.count() == 1L) {
                Optional.of(extractData(res.first()))
            } else {
                Optional.empty()
            }
        }
    }

    fun cacheQuery(_url: String, _data: String) {
        assert(tryGetCachedQuery(_url).isEmpty)
        transaction(database) {
            CacheTable.insert {
                it[url] = _url
                it[data] = _data
            }
        }
    }

    private fun extractData(rs: ResultRow) : String = rs[CacheTable.data]
}