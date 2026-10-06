package com.brewkery.app.presentation

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.brewkery.app.MainActivity
import com.brewkery.app.data.remote.toDomain
import com.brewkery.app.di.NetworkModule
import com.brewkery.app.domain.model.Money
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Live integration check: derives product names and prices from the real GitHub API. */
class BrewkeryOrderingFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun githubCatalogToOrderAndRotation() {
        val catalog = runBlocking { NetworkModule.provideApi(NetworkModule.provideHttpClient()).getMenu().toDomain() }
        assertTrue(catalog.items.isNotEmpty())
        val item = catalog.items.first()
        waitForText(item.name)
        capture("menu")
        compose.onNodeWithText(item.name).performClick()
        waitForText("ITEM CUSTOMIZER")
        waitForText("Add to Cart", substring = true)
        val size = item.customizations.sizes.lastOrNull()
        if (size != null) compose.onNodeWithText(size.label).performScrollTo().performClick()
        compose.onNodeWithContentDescription("Increase quantity").performClick()
        val unitPrice = item.basePrice + (size?.extraPrice ?: Money.Zero) +
            (item.customizations.milkOptions.firstOrNull()?.extraPrice ?: Money.Zero)
        val linePrice = (unitPrice * 2).format(catalog.store.currency)
        compose.onNode(hasScrollAction()).performScrollToIndex(0)
        capture("detail")
        compose.onNodeWithText("Add to Cart • $linePrice").performClick()
        waitForText("YOUR CART")
        compose.onNodeWithText(item.name).assertIsDisplayed()
        capture("cart")
        compose.onNodeWithText("Place Order Now", substring = true).performClick()
        waitForText("Brewing in Progress!")
        compose.onNodeWithText("PREPARING").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("2 Items").performScrollTo().assertIsDisplayed()
        val ticket = compose.onNodeWithText("#BK-", substring = true).fetchSemanticsNode()
            .config[SemanticsProperties.Text].first().text
        assertTrue(ticket.matches(Regex("#BK-[0-9]{5}")))
        capture("order")
        compose.onNodeWithText("Back to Menu").performClick()
        waitForText(ticket)
        compose.activityRule.scenario.recreate()
        waitForText(ticket)
        compose.onNodeWithContentDescription("View Your Cart").performClick()
        waitForText("Your cart is empty.")
    }

    private fun waitForText(text: String, substring: Boolean = false) {
        compose.waitUntil(45_000) { compose.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun capture(name: String) {
        compose.waitUntil(45_000) {
            compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).fetchSemanticsNodes().isEmpty()
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val directory = if (output != null) File(output, "screenshots")
            else File(context.externalMediaDirs.firstOrNull() ?: context.getExternalFilesDir(null), "additional_test_output/screenshots")
        check(directory.exists() || directory.mkdirs())
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
