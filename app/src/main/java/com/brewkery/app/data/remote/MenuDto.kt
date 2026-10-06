package com.brewkery.app.data.remote

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MenuDto(val meta: StoreDto?, val categories: List<CategoryDto>?, val items: List<MenuItemDto>?)
data class StoreDto(
    val app: String?, val tagline: String?, val currency: String?,
    @SerializedName("delivery_fee") val deliveryFee: BigDecimal?,
    @SerializedName("tax_rate_percent") val taxRatePercent: BigDecimal?,
    @SerializedName("estimated_delivery_time") val estimatedDeliveryTime: String?,
)
data class CategoryDto(val id: String?, val name: String?, val icon: String?)
data class MenuItemDto(
    val id: Int?,
    @SerializedName("category_id") val categoryId: String?,
    val name: String?, val tagline: String?, val description: String?,
    @SerializedName("base_price") val basePrice: BigDecimal?,
    val rating: BigDecimal?,
    @SerializedName("review_count") val reviewCount: Int?,
    @SerializedName("image_url") val imageUrl: String?,
    val badge: String?, val ingredients: List<String>?, val customizations: CustomizationsDto?,
)
data class CustomizationsDto(
    val sizes: List<SizeDto>?,
    @SerializedName("milk_options") val milkOptions: List<MilkOptionDto>?,
    @SerializedName("sugar_levels") val sugarLevels: List<String>?,
)
data class SizeDto(val id: String?, val label: String?, @SerializedName("extra_price") val extraPrice: BigDecimal?)
data class MilkOptionDto(val id: String?, val name: String?, @SerializedName("extra_price") val extraPrice: BigDecimal?)
