package com.linkpreview.tarasov.processor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.singleton
import org.slf4j.event.Level
import java.nio.channels.UnresolvedAddressException

/**
 * Controller responsible for `/query` handler.
 */
fun Application.queryModule() {
    val queryService: QueryService by closestDI().instance()

    installKtorExtensions()

    routing {
        route("/query") {
            val urlName = "url"
            post("/{$urlName}") {
                val url = call.parameters[urlName]
                if (url !== null) {
                    val requestParams = call.receive<RequestParams>()
                    try {
                        val queryResult = queryService.process(url, requestParams)
                        call.respond(queryResult)
                    } catch (e: UnresolvedAddressException) {
                        call.respond(queryService.getUnresolvedMessage())
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}

fun Application.installKtorExtensions() {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)

        header(HttpHeaders.Authorization)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.XForwardedProto)

        allowNonSimpleContentTypes = true
        allowCredentials = true
        allowSameOrigin = true

        host("*", listOf("http", "https"))
        anyHost()
    }

    install(CallLogging) {
        level = Level.INFO
        format { call -> "User-Agent: ${call.request.headers["User-Agent"].toString()}" }
    }
}

fun DI.Builder.queryComponents() {
    bind<QueryService>() with singleton {
        QueryService(instance(), instance())
    }

    bind<QueryDao>() with singleton {
        QueryDao(instance())
    }
}
