package com.brewkery.app.domain.repository

import com.brewkery.app.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val state: StateFlow<SessionState>
    fun setStore(store: StoreConfiguration)
    fun addItem(item: MenuItem, selection: ItemSelection, quantity: Int, unitPrice: Money)
    fun changeQuantity(lineId: String, delta: Int)
    fun clearCart()
    fun toggleFavorite(itemId: Int)
    fun placeOrder(): PlacedOrder?
}
