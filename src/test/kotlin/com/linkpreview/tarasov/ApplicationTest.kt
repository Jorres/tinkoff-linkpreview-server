package com.linkpreview.tarasov

import com.linkpreview.tarasov.processor.RequestParams
import com.linkpreview.tarasov.processor.queryComponents
import com.linkpreview.tarasov.processor.queryModule
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.application.*
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.kodein.di.ktor.di

class ApplicationTest {

    @Test
    fun testSimpleQuery() {
        val config = ConfigFactory.load().extract<AppConfig>()
        withTestApplication({ prepareApplication(config) }) {
            val emptyParams = RequestParams(
                video = false,
                img = false,
                text = false
            )
            handleRequest(HttpMethod.Post, "/query/google.com") {
                val body = Json.encodeToString(emptyParams)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentLength, body.length.toString())
                setBody(body)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val resp = Json.parseToJsonElement(response.content.toString())
                assertTrue {
                    resp.jsonObject.keys.contains("httpStatusCode")
                    resp.jsonObject.keys.contains("httpPageSize")
                    resp.jsonObject.keys.contains("title")
                    resp.jsonObject.keys.contains("description")
                    resp.jsonObject.keys.contains("locale")
                }
            }
        }
    }

    @Test
    fun testFullQuery() {
        val config = ConfigFactory.load().extract<AppConfig>()
        withTestApplication({ prepareApplication(config) }) {
            val fullParams = RequestParams(
                video = true,
                img = true,
                text = true
            )
            handleRequest(HttpMethod.Post, "/query/google.com") {
                val body = Json.encodeToString(fullParams)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentLength, body.length.toString())
                setBody(body)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val resp = Json.parseToJsonElement(response.content.toString())
                assertTrue {
                    resp.jsonObject.keys.contains("httpStatusCode")
                    resp.jsonObject.keys.contains("httpPageSize")
                    resp.jsonObject.keys.contains("title")
                    resp.jsonObject.keys.contains("description")
                    resp.jsonObject.keys.contains("locale")
                }

                assertTrue {
                    resp.jsonObject.keys.contains("imageData")
                    resp.jsonObject["imageData"]!!.jsonObject.keys.contains("totalImages")
                    resp.jsonObject.keys.contains("textData")
                    resp.jsonObject["textData"]!!.jsonObject.keys.contains("numberOfParagraphs")
                    resp.jsonObject["textData"]!!.jsonObject.keys.contains("numberOfCharacters")
                    resp.jsonObject.keys.contains("videoData")
                }
            }
        }
    }

    @Test
    fun testTextQuery() {
        val config = ConfigFactory.load().extract<AppConfig>()
        withTestApplication({ prepareApplication(config) }) {
            val fullParams = RequestParams(
                video = false,
                img = false,
                text = true
            )
            handleRequest(HttpMethod.Post, "/query/google.com") {
                val body = Json.encodeToString(fullParams)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentLength, body.length.toString())
                setBody(body)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val resp = Json.parseToJsonElement(response.content.toString())
                assertTrue {
                    resp.jsonObject.keys.contains("httpStatusCode")
                    resp.jsonObject.keys.contains("httpPageSize")
                    resp.jsonObject.keys.contains("title")
                    resp.jsonObject.keys.contains("description")
                    resp.jsonObject.keys.contains("locale")
                }

                assertTrue {
                    !resp.jsonObject.keys.contains("imageData")
                    !resp.jsonObject.keys.contains("videoData")

                    resp.jsonObject.keys.contains("textData")
                    resp.jsonObject["textData"]!!.jsonObject.keys.contains("numberOfParagraphs")
                    resp.jsonObject["textData"]!!.jsonObject.keys.contains("numberOfCharacters")
                }
            }
        }
    }

    private fun Application.prepareApplication(config: AppConfig) {
        di {
            mainComponents(config)
            queryComponents()
        }
        configureSerialization()
        queryModule()
    }
}