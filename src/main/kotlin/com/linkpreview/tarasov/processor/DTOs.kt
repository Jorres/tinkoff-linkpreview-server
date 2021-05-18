package com.linkpreview.tarasov.processor

import kotlinx.serialization.Serializable

/**
 * Result of html page parsing by [QueryService].
 */
@Serializable
data class PageData(
    var httpStatusCode: Int = 0,
    var htmlPageSize: Int = 0,
    var pageText: String = "",
    var title: String? = "Page has no title tag",
    var locale: String? = "The site did not provide meta locale information",
    var description: String? = "The site did not provide meta description information",
    var imageData: ImageData = ImageData(0),
    var videoData: VideoData = VideoData(0, listOf()),
    var textData: TextData = TextData(0, 0)
)

@Serializable
data class ImageData(val totalImages: Int)

@Serializable
data class VideoData(val totalVideos: Int, val videoSources: List<String>)

@Serializable
data class TextData(val numberOfParagraphs: Int, val numberOfCharacters: Int)

/**
 * Used to communicate between client and server.
 * Describes a subset of required information.
 */
@Serializable
data class RequestParams(
    val video: Boolean,
    val img: Boolean,
    val text: Boolean
)