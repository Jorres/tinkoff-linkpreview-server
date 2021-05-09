package com.linkpreview.tarasov.processor

import kotlinx.serialization.Serializable

@Serializable
data class PageData(
    var httpStatusCode: Int = 0,
    var htmlPageSize: Int = 0,
    var imageData: ImageData = ImageData(0),
    var videoData: VideoData = VideoData(0),
    var accessibilityData: AccessibilityData = AccessibilityData(0.0),
    var textData: TextData = TextData(0)
)

@Serializable
data class ImageData(val totalImages: Int)

@Serializable
data class VideoData(val totalVideos: Int)

@Serializable
data class AccessibilityData(val accessibilityScore: Double)

@Serializable
data class TextData(val numberOfCharacters: Int)

@Serializable
data class RequestParams(
    val accessibility: Boolean,
    val video: Boolean,
    val img: Boolean,
    val text: Boolean
)