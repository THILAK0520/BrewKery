package com.brewkery.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewkery.app.R
import com.brewkery.app.domain.model.*
import com.brewkery.app.domain.repository.BrewkeryRepository
import com.brewkery.app.domain.repository.SessionRepository
import com.brewkery.app.domain.usecase.AddCartItemUseCase
import com.brewkery.app.domain.usecase.CalculateItemPriceUseCase
import com.brewkery.app.presentation.common.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val item: MenuItem? = null,
    val selection: ItemSelection = ItemSelection(null, null, null),
    val quantity: Int = 1,
    val totalPrice: Money = Money.Zero,
    val currency: String? = null,
    val isFavorite: Boolean = false,
    val isAdding: Boolean = false,
)
sealed interface ItemDetailIntent {
    data object Retry : ItemDetailIntent
    data object ToggleFavorite : ItemDetailIntent
    data object AddToCart : ItemDetailIntent
    data class SelectSize(val id: String) : ItemDetailIntent
    data class SelectMilk(val id: String) : ItemDetailIntent
    data class SelectSugar(val label: String) : ItemDetailIntent
    data class ChangeQuantity(val delta: Int) : ItemDetailIntent
}

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BrewkeryRepository,
    private val session: SessionRepository,
    private val calculatePrice: CalculateItemPriceUseCase,
    private val addItem: AddCartItemUseCase,
) : ViewModel() {
    private val itemId: Int = checkNotNull(savedStateHandle["itemId"])
    private val mutableState = MutableStateFlow(ItemDetailUiState())
    val state = mutableState.asStateFlow()
    private val effectChannel = Channel<UiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var loadJob: Job? = null
    init {
        viewModelScope.launch { session.state.collect { current ->
            mutableState.update { it.copy(currency = current.store?.currency, isFavorite = itemId in current.favoriteItemIds) }
        } }
        loadItem()
    }
    fun accept(intent: ItemDetailIntent) {
        when (intent) {
            ItemDetailIntent.Retry -> loadItem()
            ItemDetailIntent.ToggleFavorite -> session.toggleFavorite(itemId)
            ItemDetailIntent.AddToCart -> {
                val current = state.value
                val item = current.item ?: return
                if (current.isAdding || current.currency == null) return
                mutableState.update { it.copy(isAdding = true) }
                try {
                    addItem(item, current.selection, current.quantity)
                    effectChannel.trySend(UiEffect.OpenCart)
                } catch (_: IllegalArgumentException) {
                    mutableState.update { it.copy(isAdding = false) }
                    effectChannel.trySend(UiEffect.Message(R.string.action_failed))
                }
            }
            is ItemDetailIntent.ChangeQuantity -> {
                if (intent.delta != -1 && intent.delta != 1) return
                if (state.value.quantity == 99 && intent.delta > 0) effectChannel.trySend(UiEffect.Message(R.string.quantity_limit))
                updateSelection { it.copy(quantity = (it.quantity + intent.delta).coerceIn(1, 99)) }
            }
            is ItemDetailIntent.SelectSize -> if (state.value.item?.customizations?.sizes?.any { it.id == intent.id } == true)
                updateSelection { it.copy(selection = it.selection.copy(sizeId = intent.id)) }
            is ItemDetailIntent.SelectMilk -> if (state.value.item?.customizations?.milkOptions?.any { it.id == intent.id } == true)
                updateSelection { it.copy(selection = it.selection.copy(milkOptionId = intent.id)) }
            is ItemDetailIntent.SelectSugar -> if (intent.label in state.value.item?.customizations?.sugarLevels.orEmpty())
                updateSelection { it.copy(selection = it.selection.copy(sugarLevel = intent.label)) }
        }
    }
    private fun updateSelection(reduce: (ItemDetailUiState) -> ItemDetailUiState) {
        mutableState.update { old ->
            if (old.isAdding) old else reduce(old).let { next ->
                next.item?.let { next.copy(totalPrice = calculatePrice(it, next.selection) * next.quantity) } ?: next
            }
        }
    }
    private fun loadItem() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, hasError = false) }
            try {
                // Recreated detail destinations need fresh metadata after process termination.
                if (session.state.value.store == null) session.setStore(repository.getMenu().store)
                val item = repository.getItem(itemId)
                val selection = item.defaultSelection()
                mutableState.update { it.copy(isLoading = false, item = item, selection = selection,
                    quantity = 1, totalPrice = calculatePrice(item, selection), isAdding = false) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutableState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }
}
