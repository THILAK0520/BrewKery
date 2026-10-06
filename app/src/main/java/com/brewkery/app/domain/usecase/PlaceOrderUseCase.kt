package com.brewkery.app.domain.usecase

import com.brewkery.app.domain.repository.SessionRepository
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(private val session: SessionRepository) {
    operator fun invoke() = session.placeOrder()
}
