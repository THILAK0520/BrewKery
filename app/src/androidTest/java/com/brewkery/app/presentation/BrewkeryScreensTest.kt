package com.brewkery.app.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.brewkery.app.domain.model.*
import com.brewkery.app.presentation.cart.*
import com.brewkery.app.presentation.detail.*
import com.brewkery.app.presentation.theme.BrewkeryTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class BrewkeryScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun emptyCartDisablesCheckout() {
        val store = StoreConfiguration("Test", "", "USD", Money(250), BigDecimal("8"), "20 mins")
        compose.setContent { BrewkeryTheme { CartScreen(CartUiState(session = SessionState(store = store)), {}, {}) } }
        compose.onNodeWithText("Your cart is empty.").assertIsDisplayed()
        compose.onNodeWithText("Place Order Now • $0.00").assertIsNotEnabled()
    }
    @Test fun detailUsesServerOptionsAndDispatchesSelectionIntent() {
        val item = MenuItem(7, "test", "Server Item", "", "Server description", Money(400), "4", 2,
            null, "", listOf("Server Ingredient"), Customizations(
                listOf(PricedOption("server-size", "Server Size", Money.Zero)), emptyList(), emptyList()))
        var selected: ItemDetailIntent? = null
        compose.setContent { BrewkeryTheme {
            ItemDetailScreen(ItemDetailUiState(isLoading = false, item = item, currency = "USD", totalPrice = Money(400)), { selected = it }, {})
        } }
        compose.onNodeWithText("Server Size").performScrollTo().performClick()
        assertEquals(ItemDetailIntent.SelectSize("server-size"), selected)
        compose.onNodeWithText("Add to Cart • $4.00").assertIsEnabled()
    }
}
