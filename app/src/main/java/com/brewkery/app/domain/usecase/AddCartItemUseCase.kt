package com.brewkery.app.domain.usecase

import com.brewkery.app.domain.model.*
import com.brewkery.app.domain.repository.SessionRepository
import javax.inject.Inject

class AddCartItemUseCase @Inject constructor(
    private val session: SessionRepository,
    private val calculatePrice: CalculateItemPriceUseCase,
) {
    operator fun invoke(item: MenuItem, selection: ItemSelection, quantity: Int) {
        require(quantity in 1..99)
        session.addItem(item, selection, quantity, calculatePrice(item, selection))
    }
}
