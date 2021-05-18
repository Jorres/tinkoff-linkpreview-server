package com.linkpreview.tarasov.processor

import com.linkpreview.tarasov.CachingConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import org.jsoup.Jsoup

/**
 * The business logic of an application, forming the
 * useful part of the `/query` response.
 */
class QueryService(private val queryDao: QueryDao, private val cachingConfig: CachingConfig) {
    fun getUnresolvedMessage(): JsonObject {
        return buildJsonObject {
            put("Error", Json.encodeToJsonElement(
            "Sorry, we couldn't connect with this host. " +
                 "Make sure you have a correct address in your input."
            ))
        }
    }

    suspend fun process(url: String, requestParams: RequestParams): JsonObject {
        val scrapedPage = if (cachingConfig.enabled) {
            val maybeCached = queryDao.tryGetCachedQuery(url)
            if (maybeCached.isPresent) {
                Json.decodeFromString(maybeCached.get())
            } else {
                val tmpResult = scrapePage(appendHTTPS(url))
                queryDao.cacheQuery(url, Json.encodeToString(tmpResult))
                tmpResult
            }
        } else {
            scrapePage(appendHTTPS(url))
        }

        return buildJsonObject {
            put("httpStatusCode",
                Json.encodeToJsonElement(scrapedPage.httpStatusCode))
            put("httpPageSize",
                Json.encodeToJsonElement(scrapedPage.htmlPageSize))
            put("title",
                Json.encodeToJsonElement(scrapedPage.title))
            put("description",
                Json.encodeToJsonElement(scrapedPage.description))
            put("locale",
                Json.encodeToJsonElement(scrapedPage.locale))
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
        val client = HttpClient(CIO)
        val response: HttpResponse = client.get(pageUrl)
        val pageData = response.readText()
        client.close()

        val doc = Jsoup.parse(pageData)
        val paragraphs = doc.select("p")
        val links = doc.select("a")
        val videos = doc.select("video")

        return PageData(
            httpStatusCode = response.status.value,
            htmlPageSize = pageData.length,
            pageText = pageData,
            title = doc.title(),
            locale = doc.select("html").attr("lang"),
            description = doc.select("meta")
                .filter{elem -> elem.attr("name") == "description"}
                .map { elem -> elem.attr("content") }
                .firstOrNull(),
            imageData = ImageData(
                doc.select("img").size
            ),
            videoData = VideoData(
                videos.size,
                videos.map { elem -> elem.attr("src")}
            ),
            textData = TextData(
               paragraphs.size + links.size,
                (paragraphs + links).fold(0) {sum, elem ->
                    sum + elem.ownText().length
                }
            )
        )
    }

    private fun appendHTTPS(url: String): String {
        return "https://$url"
    }
}