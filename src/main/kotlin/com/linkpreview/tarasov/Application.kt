package com.linkpreview.tarasov

import com.linkpreview.tarasov.processor.queryComponents
import com.linkpreview.tarasov.processor.queryModule
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.ktor.di
import org.kodein.di.singleton

fun main() {
    val config = ConfigFactory.load().extract<AppConfig>()
    val dbConfig = config.database

    val engine = embeddedServer(Netty, port = config.http.port) {
        di {
            mainComponents(config)
            queryComponents()
        }
        configureSerialization()
        queryModule()
    }

    if (config.database.inMemory) {
        migrate(dbConfig)
    }

    engine.start(wait = true)
}

fun migrate(dbConfig: DatabaseConfig) {
    Flyway
        .configure()
        .dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
        .load()
        .migrate()
}

fun DI.Builder.mainComponents(config: AppConfig) {
    bind<AppConfig>() with singleton {
        config
    }
    val dbConfig = config.database
    bind<Database>() with singleton {
        Database.connect(
            url = dbConfig.url,
            user = dbConfig.user,
            password = dbConfig.password
        )
    }

    bind<CachingConfig>() with singleton {
        config.caching
    }
}
