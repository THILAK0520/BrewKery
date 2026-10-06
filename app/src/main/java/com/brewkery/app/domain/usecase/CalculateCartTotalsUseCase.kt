package com.brewkery.app.domain.usecase

import com.brewkery.app.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateCartTotalsUseCase @Inject constructor() {
    operator fun invoke(lines: List<CartLine>, store: StoreConfiguration?): CartTotals {
        val subtotal = lines.fold(Money.Zero) { sum, line -> sum + line.unitPrice * line.quantity }
        if (subtotal == Money.Zero || store == null) return CartTotals(subtotal, Money.Zero, Money.Zero, subtotal)
        val taxCents = BigDecimal.valueOf(subtotal.cents).multiply(store.taxRatePercent)
            .divide(BigDecimal("100"), 0, RoundingMode.HALF_UP).longValueExact()
        val tax = Money(taxCents)
        return CartTotals(subtotal, store.deliveryFee, tax, subtotal + store.deliveryFee + tax)
    }
}
