package com.example.slideshow.util

object PlayerIndex {

    fun nextIndex(index: Int, size: Int, step: Int, loop: Boolean): Int {
        if (size <= 0) return 0
        val last = size - 1
        val raw = index + step
        return when {
            raw in 0..last -> raw
            loop -> ((raw % size) + size) % size
            else -> index.coerceIn(0, last)
        }
    }
}
