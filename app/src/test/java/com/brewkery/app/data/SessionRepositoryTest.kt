package com.brewkery.app.data

import com.brewkery.app.data.session.InMemorySessionRepository
import com.brewkery.app.domain.model.*
import com.brewkery.app.domain.usecase.*
import com.brewkery.app.testItem
import com.brewkery.app.testStore
import org.junit.Assert.*
import org.junit.Test

class SessionRepositoryTest {
    private val session = InMemorySessionRepository(CalculateCartTotalsUseCase())
    private val addItem = AddCartItemUseCase(session, CalculateItemPriceUseCase())

    @Test fun `ordering snapshots quantities and totals then clears cart`() {
        session.setStore(testStore())
        addItem(testItem(), testItem().defaultSelection(), 2)
        val order = requireNotNull(session.placeOrder())
        assertEquals(2, order.quantity)
        assertEquals(OrderStatus.PREPARING, order.status)
        assertTrue(order.ticketId.matches(Regex("#BK-[0-9]{5}")))
        assertTrue(session.state.value.cart.isEmpty())
        assertEquals(order, session.state.value.activeOrder)
        assertNull(session.placeOrder())
        addItem(testItem(), testItem().defaultSelection(), 1)
        assertEquals(2, order.quantity)
    }
    @Test fun `decrement to zero removes line and clear removes all lines`() {
        addItem(testItem(), testItem().defaultSelection(), 1)
        val id = session.state.value.cart.single().lineId
        session.changeQuantity(id, -1)
        assertTrue(session.state.value.cart.isEmpty())
        addItem(testItem(), testItem().defaultSelection(), 1)
        session.clearCart()
        assertTrue(session.state.value.cart.isEmpty())
    }
    @Test fun `different configurations remain separate cart lines`() {
        addItem(testItem(), testItem().defaultSelection(), 1)
        addItem(testItem(), ItemSelection("large", "extra", "Standard"), 1)
        assertEquals(2, session.state.value.cart.size)
        assertEquals(listOf(Money(485), Money(600)), session.state.value.cart.map { it.unitPrice })
    }
    @Test fun `quantity is capped and favorite toggles consistently`() {
        addItem(testItem(), testItem().defaultSelection(), 99)
        session.changeQuantity(session.state.value.cart.single().lineId, 1)
        assertEquals(99, session.state.value.quantity)
        session.toggleFavorite(1)
        assertTrue(1 in session.state.value.favoriteItemIds)
        session.toggleFavorite(1)
        assertFalse(1 in session.state.value.favoriteItemIds)
    }
    @Test fun `empty cart or missing store cannot place order`() {
        assertNull(session.placeOrder())
        addItem(testItem(), testItem().defaultSelection(), 1)
        assertNull(session.placeOrder())
    }
}
