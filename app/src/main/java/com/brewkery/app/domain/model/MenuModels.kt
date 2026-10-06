package com.brewkery.app.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Exact currency representation; no floating-point calculations in business rules. */
@JvmInline
value class Money(val cents: Long) {
    init { require(cents >= 0) }
    operator fun plus(other: Money) = Money(Math.addExact(cents, other.cents))
    operator fun times(quantity: Int): Money {
        require(quantity >= 0)
        return Money(Math.multiplyExact(cents, quantity.toLong()))
    }
    fun format(currency: String): String = NumberFormat.getCurrencyInstance(Locale.US).apply {
        this.currency = Currency.getInstance(currency)
    }.format(BigDecimal.valueOf(cents, 2))
    companion object {
        val Zero = Money(0)
        fun fromDecimal(value: BigDecimal): Money {
            require(value.signum() >= 0 && value <= BigDecimal("1000000"))
            return Money(value.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact())
        }
    }
}

data class StoreConfiguration(
    val appName: String,
    val tagline: String,
    val currency: String,
    val deliveryFee: Money,
    val taxRatePercent: BigDecimal,
    val estimatedDeliveryTime: String,
)
data class MenuCategory(val id: String, val name: String, val icon: String)
data class PricedOption(val id: String, val label: String, val extraPrice: Money)
data class Customizations(
    val sizes: List<PricedOption>,
    val milkOptions: List<PricedOption>,
    val sugarLevels: List<String>,
)
data class MenuItem(
    val id: Int,
    val categoryId: String,
    val name: String,
    val tagline: String,
    val description: String,
    val basePrice: Money,
    val rating: String,
    val reviewCount: Int,
    val imageUrl: String?,
    val badge: String,
    val ingredients: List<String>,
    val customizations: Customizations,
)
data class MenuCatalog(val store: StoreConfiguration, val categories: List<MenuCategory>, val items: List<MenuItem>)
data class ItemSelection(val sizeId: String?, val milkOptionId: String?, val sugarLevel: String?)

fun MenuItem.defaultSelection() = ItemSelection(
    customizations.sizes.firstOrNull()?.id,
    customizations.milkOptions.firstOrNull()?.id,
    customizations.sugarLevels.firstOrNull(),
)
