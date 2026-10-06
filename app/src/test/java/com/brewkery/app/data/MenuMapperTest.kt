package com.brewkery.app.data

import com.brewkery.app.data.remote.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class MenuMapperTest {
    private fun dto() = MenuItemDto(99, "dynamic", "Remote name", "Remote tagline", "Remote description",
        BigDecimal("3.25"), BigDecimal("4.75"), 12, "https://images.unsplash.com/test?w=800", "REMOTE",
        listOf("Remote ingredient"), CustomizationsDto(listOf(SizeDto("size", "Remote size", BigDecimal.ZERO)),
            listOf(MilkOptionDto("milk", "Remote milk", BigDecimal("0.60"))), listOf("Remote sugar")))

    @Test fun `all product information comes from DTO including arbitrary ids`() {
        val item = dto().toDomain()
        assertEquals(99, item.id)
        assertEquals("Remote name", item.name)
        assertEquals(325L, item.basePrice.cents)
        assertEquals("Remote ingredient", item.ingredients.single())
        assertEquals(60L, item.customizations.milkOptions.single().extraPrice.cents)
    }
    @Test fun `unsafe image URLs are discarded without discarding product`() {
        listOf("http://images.unsplash.com/test", "https://evil.example/test", "https://images.unsplash.com.evil.example/test",
            "https://user@images.unsplash.com/test").forEach { assertNull(dto().copy(imageUrl = it).toDomain().imageUrl) }
        assertNotNull(dto().toDomain().imageUrl)
    }
    @Test(expected = IllegalArgumentException::class) fun `negative prices are rejected`() {
        dto().copy(basePrice = BigDecimal("-1")).toDomain()
    }
    @Test(expected = IllegalArgumentException::class) fun `incomplete item is rejected rather than fabricated`() {
        dto().copy(name = null).toDomain()
    }
}
