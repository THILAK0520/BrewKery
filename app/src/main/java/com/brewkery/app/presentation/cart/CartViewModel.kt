package com.brewkery.app.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewkery.app.domain.model.*
import com.brewkery.app.domain.repository.SessionRepository
import com.brewkery.app.domain.usecase.CalculateCartTotalsUseCase
import com.brewkery.app.domain.usecase.PlaceOrderUseCase
import com.brewkery.app.presentation.common.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val session: SessionState = SessionState(),
    val totals: CartTotals = CartTotals(Money.Zero, Money.Zero, Money.Zero, Money.Zero),
    val isPlacingOrder: Boolean = false,
)
sealed interface CartIntent {
    data object Clear : CartIntent
    data object PlaceOrder : CartIntent
    data class ChangeQuantity(val lineId: String, val delta: Int) : CartIntent
}
@HiltViewModel
class CartViewModel @Inject constructor(
    private val session: SessionRepository,
    private val calculateTotals: CalculateCartTotalsUseCase,
    private val placeOrder: PlaceOrderUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CartUiState())
    val state = mutableState.asStateFlow()
    private val effectChannel = Channel<UiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    init {
        viewModelScope.launch { session.state.collect { current ->
            mutableState.update { it.copy(session = current, totals = calculateTotals(current.cart, current.store)) }
        } }
    }
    fun accept(intent: CartIntent) {
        if (state.value.isPlacingOrder) return
        when (intent) {
            CartIntent.Clear -> session.clearCart()
            is CartIntent.ChangeQuantity -> session.changeQuantity(intent.lineId, intent.delta)
            CartIntent.PlaceOrder -> {
                mutableState.update { it.copy(isPlacingOrder = true) }
                if (placeOrder() != null) effectChannel.trySend(UiEffect.OpenOrder)
                else mutableState.update { it.copy(isPlacingOrder = false) }
            }
        }
    }
}
