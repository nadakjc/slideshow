package com.example.slideshow.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerIndexTest {

    @Test
    fun `moves forward inside range`() {
        assertEquals(1, PlayerIndex.nextIndex(index = 0, size = 5, step = 1, loop = false))
        assertEquals(4, PlayerIndex.nextIndex(index = 3, size = 5, step = 1, loop = false))
    }

    @Test
    fun `moves backward inside range`() {
        assertEquals(2, PlayerIndex.nextIndex(index = 3, size = 5, step = -1, loop = false))
        assertEquals(0, PlayerIndex.nextIndex(index = 1, size = 5, step = -1, loop = false))
    }

    @Test
    fun `loop wraps forward past the last item`() {
        assertEquals(0, PlayerIndex.nextIndex(index = 4, size = 5, step = 1, loop = true))
    }

    @Test
    fun `loop wraps backward before the first item`() {
        assertEquals(4, PlayerIndex.nextIndex(index = 0, size = 5, step = -1, loop = true))
    }

    @Test
    fun `without loop it clamps at the edges`() {
        assertEquals(4, PlayerIndex.nextIndex(index = 4, size = 5, step = 1, loop = false))
        assertEquals(0, PlayerIndex.nextIndex(index = 0, size = 5, step = -1, loop = false))
    }

    @Test
    fun `handles single item collection`() {
        assertEquals(0, PlayerIndex.nextIndex(index = 0, size = 1, step = 1, loop = true))
        assertEquals(0, PlayerIndex.nextIndex(index = 0, size = 1, step = -1, loop = true))
        assertEquals(0, PlayerIndex.nextIndex(index = 0, size = 1, step = 1, loop = false))
    }

    @Test
    fun `handles empty collection`() {
        assertEquals(0, PlayerIndex.nextIndex(index = 0, size = 0, step = 1, loop = true))
        assertEquals(0, PlayerIndex.nextIndex(index = 5, size = 0, step = -1, loop = false))
    }

    @Test
    fun `large jumps with loop wrap correctly`() {
        assertEquals(2, PlayerIndex.nextIndex(index = 4, size = 5, step = 3, loop = true))
        assertEquals(3, PlayerIndex.nextIndex(index = 0, size = 5, step = -2, loop = true))
    }
}
