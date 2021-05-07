package com.linkpreview.tarasov.processor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.engine.cio.*
import io.ktor.client.statement.*

class QueryService(private val queryDao: QueryDao) {
    suspend fun process(uri: String, requestParams: RequestParams): PageData {
        val maybeCached = queryDao.tryGetCachedQuery(uri)
        if (maybeCached.isPresent) {
            println("query cached, returning cached value...")
            val cached = maybeCached.get()
            println(cached)
            return PageData(cached)
        }
        println("query not cached, performing request...")

        // TODO check if you need more features from here
        // https://ktor.io/docs/client.html#make-request0

        val client = HttpClient(CIO)

        val pageData: String = client.get("https://$uri")
        client.close()

        queryDao.cacheQuery(uri, pageData)

        return PageData(pageData)
    }
}