package com.example.slideshow.data

object ImageFilter {

    val IMAGE_EXTENSIONS: Set<String> = setOf(
        "jpg", "jpeg", "png", "webp", "heic", "heif", "gif", "bmp",
    )

    fun isImage(mime: String?, name: String?): Boolean {
        if (mime != null && mime.startsWith("image/")) return true
        val ext = name?.substringAfterLast('.', "")?.lowercase() ?: return false
        return ext.isNotEmpty() && ext in IMAGE_EXTENSIONS
    }
}
