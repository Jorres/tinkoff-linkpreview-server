package com.linkpreview.tarasov.processor

import kotlinx.serialization.Serializable

@Serializable
data class PageData(val pageUrl: String)

@Serializable
data class RequestParams(
    val accessibility: Boolean,
    val video: Boolean,
    val img: Boolean,
    val text: Boolean
)