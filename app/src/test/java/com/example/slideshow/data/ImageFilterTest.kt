package com.example.slideshow.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageFilterTest {

    @Test
    fun `accepts image mime types regardless of name`() {
        assertTrue(ImageFilter.isImage(mime = "image/jpeg", name = null))
        assertTrue(ImageFilter.isImage(mime = "image/png", name = "photo"))
        assertTrue(ImageFilter.isImage(mime = "image/heif", name = "noext"))
    }

    @Test
    fun `rejects non-image mime types without extension fallback`() {
        assertFalse(ImageFilter.isImage(mime = "video/mp4", name = null))
        assertFalse(ImageFilter.isImage(mime = "application/pdf", name = "doc.pdf"))
        assertFalse(ImageFilter.isImage(mime = "text/plain", name = "readme.txt"))
    }

    @Test
    fun `falls back to extension when mime is missing`() {
        assertTrue(ImageFilter.isImage(mime = null, name = "holiday.JPG"))
        assertTrue(ImageFilter.isImage(mime = null, name = "cat.png"))
        assertTrue(ImageFilter.isImage(mime = null, name = "old.Bmp"))
        assertTrue(ImageFilter.isImage(mime = null, name = "animated.gif"))
        assertTrue(ImageFilter.isImage(mime = null, name = "snap.heic"))
        assertTrue(ImageFilter.isImage(mime = null, name = "vector.webp"))
    }

    @Test
    fun `rejects unknown extensions and missing names`() {
        assertFalse(ImageFilter.isImage(mime = null, name = "song.mp3"))
        assertFalse(ImageFilter.isImage(mime = null, name = "archive.zip"))
        assertFalse(ImageFilter.isImage(mime = null, name = "noext"))
        assertFalse(ImageFilter.isImage(mime = null, name = ""))
        assertFalse(ImageFilter.isImage(mime = null, name = null))
    }

    @Test
    fun `extension set is stable`() {
        assertEquals(
            setOf("jpg", "jpeg", "png", "webp", "heic", "heif", "gif", "bmp"),
            ImageFilter.IMAGE_EXTENSIONS,
        )
    }
}
