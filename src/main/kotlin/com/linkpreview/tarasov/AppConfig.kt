package com.linkpreview.tarasov

data class AppConfig(
    val http: HttpConfig,
    val database: DatabaseConfig
)

data class HttpConfig(val port: Int)

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String
)
