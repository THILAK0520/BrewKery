package com.brewkery.app.data.session

import com.brewkery.app.domain.model.*
import com.brewkery.app.domain.repository.SessionRepository
import com.brewkery.app.domain.usecase.CalculateCartTotalsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** One process-scoped source of truth. Ordering snapshots and clears the cart atomically. */
@Singleton
class InMemorySessionRepository @Inject constructor(
    private val calculateTotals: CalculateCartTotalsUseCase,
) : SessionRepository {
    private val mutableState = MutableStateFlow(SessionState())
    override val state = mutableState.asStateFlow()
    private val random = SecureRandom()

    override fun setStore(store: StoreConfiguration) { mutableState.update { it.copy(store = store) } }
    override fun addItem(item: MenuItem, selection: ItemSelection, quantity: Int, unitPrice: Money) {
        require(quantity in 1..99)
        val line = CartLine(UUID.randomUUID().toString(), item.id, item.name, selection,
            item.customizations.sizes.find { it.id == selection.sizeId }?.label,
            item.customizations.milkOptions.find { it.id == selection.milkOptionId }?.label,
            unitPrice, quantity)
        mutableState.update { it.copy(cart = it.cart + line) }
    }
    override fun changeQuantity(lineId: String, delta: Int) {
        require(delta == -1 || delta == 1)
        mutableState.update { session ->
            session.copy(cart = session.cart.mapNotNull { line ->
                if (line.lineId != lineId) line else {
                    val quantity = (line.quantity + delta).coerceAtMost(99)
                    if (quantity <= 0) null else line.copy(quantity = quantity)
                }
            })
        }
    }
    override fun clearCart() { mutableState.update { it.copy(cart = emptyList()) } }
    override fun toggleFavorite(itemId: Int) {
        mutableState.update { it.copy(favoriteItemIds = if (itemId in it.favoriteItemIds)
            it.favoriteItemIds - itemId else it.favoriteItemIds + itemId) }
    }
    override fun placeOrder(): PlacedOrder? {
        while (true) {
            val before = mutableState.value
            val store = before.store ?: return null
            if (before.cart.isEmpty()) return null
            val order = PlacedOrder("#BK-${10000 + random.nextInt(90000)}", before.cart.toList(),
                calculateTotals(before.cart, store), store.estimatedDeliveryTime)
            if (mutableState.compareAndSet(before, before.copy(cart = emptyList(), activeOrder = order))) return order
        }
    }
}
