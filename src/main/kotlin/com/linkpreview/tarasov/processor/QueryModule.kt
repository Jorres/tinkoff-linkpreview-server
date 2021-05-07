package com.linkpreview.tarasov.processor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.singleton
import org.slf4j.event.Level

fun Application.queryModule() {
    val queryService: QueryService by closestDI().instance()

    /*
     * A temporary crutch to enable local development to circumvent CORS.
     * TODO find out how to setup it properly
     */
    install(CORS) {
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)
        anyHost()
        header(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }

    install(CallLogging) {
        level = Level.INFO
        format { call -> "User-Agent: ${call.request.headers["User-Agent"].toString()}" }
    }

    routing {
        route("/query") {
            val urlName = "url"
            post("/{$urlName}") {
                val url = call.parameters[urlName]
                if (url !== null) {
                    val requestParams = call.receive<RequestParams>()
                    val queryResult = queryService.process(url, requestParams)
                    call.respond(queryResult)
                    println(queryResult)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}

fun DI.Builder.queryComponents() {
    bind<QueryService>() with singleton {
        QueryService(instance())
    }

    bind<QueryDao>() with singleton {
        QueryDao(instance())
    }
}
