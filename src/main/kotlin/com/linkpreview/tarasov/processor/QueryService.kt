package com.linkpreview.tarasov.processor

import it.skrape.core.document
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.skrape
import it.skrape.selects.eachImage
import it.skrape.selects.eachSrc
import it.skrape.selects.eachText
import it.skrape.selects.html5.img
import it.skrape.selects.html5.p
import it.skrape.selects.html5.video
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

class QueryService(private val queryDao: QueryDao) {
    suspend fun process(url: String, requestParams: RequestParams): JsonObject {
        val maybeCached = queryDao.tryGetCachedQuery(url)
        val scrapedPage = if (maybeCached.isPresent) {
            println("query cached, returning cached value...")
            Json.decodeFromString(maybeCached.get())
        } else {
            println("query not cached, performing request...")
            val tmpResult = scrapePage("https://$url")
            queryDao.cacheQuery(url, Json.encodeToString(tmpResult))
            tmpResult
        }

        return buildJsonObject {
            put("httpStatusCode",
                Json.encodeToJsonElement(scrapedPage.httpStatusCode))
            put("httpPageSize",
                Json.encodeToJsonElement(scrapedPage.htmlPageSize))
            if (requestParams.accessibility) {
                put("accessibilityData",
                    Json.encodeToJsonElement(scrapedPage.accessibilityData))
            }
            if (requestParams.video) {
                put("videoData",
                    Json.encodeToJsonElement(scrapedPage.videoData))
            }
            if (requestParams.img) {
                put("imageData",
                    Json.encodeToJsonElement(scrapedPage.imageData))
            }
            if (requestParams.text) {
                put("textData",
                    Json.encodeToJsonElement(scrapedPage.textData))
            }
        }
    }

    private suspend fun scrapePage(pageUrl: String): PageData {
        return skrape(AsyncFetcher) {
            request {
                url = pageUrl
            }
            extractIt {
                it.httpStatusCode = status { code }
                it.htmlPageSize = responseBody.length
                it.accessibilityData = AccessibilityData(3.14)
                it.imageData = ImageData(
                    catchZeroSize {
                        document.img {
                            findAll{ eachImage }.size
                        }
                    }
                )
                it.textData = TextData(
                    catchZeroSize {
                        document.p {
                            findAll { eachText }.fold(0) { a, s ->
                                a + s.length
                            }
                        }
                    }
                )
                it.videoData = VideoData(
                    catchZeroSize {
                        document.video {
                            findAll { eachSrc }.size
                        }
                    }
                )
            }
        }
    }

    private fun catchZeroSize(action: () -> Int): Int {
        return try {
            action()
        } catch (e: Exception) {
            0
        }
    }
}