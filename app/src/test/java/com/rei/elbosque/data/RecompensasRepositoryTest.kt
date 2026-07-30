package com.rei.elbosque.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RecompensasRepositoryTest {
    @Test fun `no entrega sticker antes de cinco estrellas`() {
        assertEquals(0, calcularStickers(0))
        assertEquals(0, calcularStickers(4))
    }

    @Test fun `entrega un sticker por cada cinco estrellas`() {
        assertEquals(1, calcularStickers(5))
        assertEquals(2, calcularStickers(10))
        assertEquals(5, calcularStickers(25))
    }

    @Test fun `nunca desbloquea mas stickers que los disponibles`() {
        assertEquals(5, calcularStickers(500))
    }
}
