package com.brewkery.app.domain

import com.brewkery.app.domain.model.*
import com.brewkery.app.domain.usecase.*
import com.brewkery.app.testItem
import com.brewkery.app.testStore
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class PricingTest {
    private val itemPrice = CalculateItemPriceUseCase()
    private val cartTotals = CalculateCartTotalsUseCase()

    @Test fun `selected size and milk surcharges are included exactly`() {
        assertEquals(Money(600), itemPrice(testItem(), ItemSelection("large", "extra", "Standard")))
        assertEquals(Money(1800), itemPrice(testItem(), ItemSelection("large", "extra", "Standard")) * 3)
    }
    @Test fun `sugar labels never introduce undocumented surcharges`() {
        val item = testItem().copy(customizations = testItem().customizations.copy(sugarLevels = listOf("Honey (+0.40)")))
        assertEquals(Money(485), itemPrice(item, ItemSelection("small", "plain", "Honey (+0.40)")))
    }
    @Test(expected = IllegalArgumentException::class) fun `unknown customization is rejected`() {
        itemPrice(testItem(), ItemSelection("missing", "plain", "Standard"))
    }
    @Test fun `reference subtotal gives 75 cents tax and 1265 cents total`() {
        val line = CartLine("1", 1, "Test", ItemSelection(null, null, null), null, null, Money(940), 1)
        assertEquals(CartTotals(Money(940), Money(250), Money(75), Money(1265)), cartTotals(listOf(line), testStore()))
    }
    @Test fun `empty cart has no delivery or tax`() {
        assertEquals(CartTotals(Money.Zero, Money.Zero, Money.Zero, Money.Zero), cartTotals(emptyList(), testStore()))
    }
    @Test fun `tax half cent rounds up and money is precise`() {
        val line = CartLine("1", 1, "Test", ItemSelection(null, null, null), null, null, Money(5), 1)
        assertEquals(Money(1), cartTotals(listOf(line), testStore().copy(taxRatePercent = BigDecimal("10"))).tax)
        assertEquals(Money(101), Money.fromDecimal(BigDecimal("1.005")))
    }
}
