package com.linkpreview.tarasov.processor

import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * Stores raw results of previously performed queries.
 */
object CacheTable : IntIdTable() {
    val url = text("url")
    val data = text("data")
}