package com.brewkery.app

import com.brewkery.app.domain.model.*
import java.math.BigDecimal

/** Synthetic test data only; never packaged into the app's catalog. */
fun testStore() = StoreConfiguration("Test Store", "", "USD", Money(250), BigDecimal("8"), "20 - 30 mins")
fun testItem() = MenuItem(1, "test", "Test Item", "", "Test description", Money(485), "4.9", 1,
    "https://images.unsplash.com/test", "", emptyList(), Customizations(
        listOf(PricedOption("small", "Small", Money.Zero), PricedOption("large", "Large", Money(65))),
        listOf(PricedOption("plain", "Plain", Money.Zero), PricedOption("extra", "Extra", Money(50))),
        listOf("Standard", "Unsweetened")))
