package com.brewkery.app.data.remote

import com.brewkery.app.domain.model.*
import java.math.BigDecimal
import java.net.URI
import java.util.Currency

private fun String?.required() = requireNotNull(this).also { require(it.isNotBlank()) }

fun MenuDto.toDomain(): MenuCatalog {
    val config = requireNotNull(meta)
    val currency = config.currency.required().also { Currency.getInstance(it) }
    val tax = requireNotNull(config.taxRatePercent).also { require(it in BigDecimal.ZERO..BigDecimal("100")) }
    val categories = requireNotNull(categories).map { MenuCategory(it.id.required(), it.name.required(), it.icon.orEmpty()) }
    val items = requireNotNull(items).map { it.toDomain() }
    require(categories.map { it.id }.distinct().size == categories.size)
    require(items.map { it.id }.distinct().size == items.size)
    require(items.all { item -> categories.any { it.id == item.categoryId } })
    return MenuCatalog(StoreConfiguration(config.app.required(), config.tagline.orEmpty(), currency,
        Money.fromDecimal(requireNotNull(config.deliveryFee)), tax, config.estimatedDeliveryTime.required()), categories, items)
}

fun MenuItemDto.toDomain(): MenuItem {
    val itemId = requireNotNull(id).also { require(it > 0) }
    val customization = requireNotNull(customizations)
    val sizes = customization.sizes.orEmpty().map { PricedOption(it.id.required(), it.label.required(), Money.fromDecimal(requireNotNull(it.extraPrice))) }
    val milks = customization.milkOptions.orEmpty().map { PricedOption(it.id.required(), it.name.required(), Money.fromDecimal(requireNotNull(it.extraPrice))) }
    require(sizes.map { it.id }.distinct().size == sizes.size)
    require(milks.map { it.id }.distinct().size == milks.size)
    val image = imageUrl?.takeIf { url -> runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.host == "images.unsplash.com" && uri.userInfo == null && uri.port == -1
    }.getOrDefault(false) }
    val itemRating = requireNotNull(rating).also { require(it in BigDecimal.ZERO..BigDecimal("5")) }
    return MenuItem(itemId, categoryId.required(), name.required(), tagline.orEmpty(), description.required(),
        Money.fromDecimal(requireNotNull(basePrice)), itemRating.stripTrailingZeros().toPlainString(),
        requireNotNull(reviewCount).also { require(it >= 0) }, image, badge.orEmpty(), ingredients.orEmpty(),
        Customizations(sizes, milks, customization.sugarLevels.orEmpty()))
}
