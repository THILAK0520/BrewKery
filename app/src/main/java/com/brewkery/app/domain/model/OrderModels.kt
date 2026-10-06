package com.brewkery.app.domain.model

data class CartLine(
    val lineId: String,
    val itemId: Int,
    val name: String,
    val selection: ItemSelection,
    val sizeLabel: String?,
    val milkLabel: String?,
    val unitPrice: Money,
    val quantity: Int,
)
data class CartTotals(val subtotal: Money, val delivery: Money, val tax: Money, val total: Money)
enum class OrderStatus { PREPARING }
data class PlacedOrder(
    val ticketId: String,
    val lines: List<CartLine>,
    val totals: CartTotals,
    val estimatedWait: String,
    val status: OrderStatus = OrderStatus.PREPARING,
) {
    val quantity: Int get() = lines.sumOf { it.quantity }
}
data class SessionState(
    val store: StoreConfiguration? = null,
    val cart: List<CartLine> = emptyList(),
    val activeOrder: PlacedOrder? = null,
    val favoriteItemIds: Set<Int> = emptySet(),
) {
    val quantity: Int get() = cart.sumOf { it.quantity }
}
