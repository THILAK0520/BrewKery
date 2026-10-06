package com.brewkery.app.domain.usecase

import com.brewkery.app.domain.model.*
import javax.inject.Inject

class CalculateItemPriceUseCase @Inject constructor() {
    operator fun invoke(item: MenuItem, selection: ItemSelection): Money {
        val sizes = item.customizations.sizes
        val milks = item.customizations.milkOptions
        require(sizes.isEmpty() && selection.sizeId == null || sizes.any { it.id == selection.sizeId })
        require(milks.isEmpty() && selection.milkOptionId == null || milks.any { it.id == selection.milkOptionId })
        require(item.customizations.sugarLevels.isEmpty() && selection.sugarLevel == null ||
            selection.sugarLevel in item.customizations.sugarLevels)
        return item.basePrice + (sizes.find { it.id == selection.sizeId }?.extraPrice ?: Money.Zero) +
            (milks.find { it.id == selection.milkOptionId }?.extraPrice ?: Money.Zero)
    }
}
