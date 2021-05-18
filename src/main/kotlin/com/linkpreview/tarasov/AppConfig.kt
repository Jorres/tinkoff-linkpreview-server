package com.linkpreview.tarasov

data class AppConfig(
    val http: HttpConfig,
    val database: DatabaseConfig,
    val caching: CachingConfig
)

data class HttpConfig(val port: Int)

data class CachingConfig(val enabled: Boolean)

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val inMemory: Boolean
)
