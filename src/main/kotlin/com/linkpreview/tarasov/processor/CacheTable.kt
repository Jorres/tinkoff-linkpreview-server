package com.linkpreview.tarasov.processor

import org.jetbrains.exposed.dao.id.IntIdTable

object CacheTable : IntIdTable() {
    val url = text("url")
    val data = text("data")
}