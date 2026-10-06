package com.brewkery.app.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewkery.app.domain.model.PlacedOrder
import com.brewkery.app.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class OrderStatusUiState(val order: PlacedOrder? = null)

@HiltViewModel
class OrderStatusViewModel @Inject constructor(session: SessionRepository) : ViewModel() {
    val state = session.state.map { OrderStatusUiState(it.activeOrder) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderStatusUiState(session.state.value.activeOrder))
}
