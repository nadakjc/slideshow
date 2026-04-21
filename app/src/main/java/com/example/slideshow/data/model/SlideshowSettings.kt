package com.example.slideshow.data.model

data class SlideshowSettings(
    val folderUri: String? = null,
    val intervalMs: Long = 5_000L,
    val shuffle: Boolean = false,
    val loop: Boolean = true,
    val recursive: Boolean = true,
)
