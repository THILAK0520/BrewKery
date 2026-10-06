package com.brewkery.app.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewkery.app.domain.model.*
import com.brewkery.app.domain.repository.BrewkeryRepository
import com.brewkery.app.domain.repository.SessionRepository
import com.brewkery.app.domain.usecase.CalculateCartTotalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val catalog: MenuCatalog? = null,
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val session: SessionState = SessionState(),
    val cartSubtotal: Money = Money.Zero,
) {
    val visibleItems: List<MenuItem> get() = catalog?.items.orEmpty().filter {
        (selectedCategoryId == null || it.categoryId == selectedCategoryId) &&
            (searchQuery.isBlank() || it.name.contains(searchQuery.trim(), true) ||
                it.description.contains(searchQuery.trim(), true))
    }
}
sealed interface MenuIntent {
    data object Retry : MenuIntent
    data class SearchChanged(val query: String) : MenuIntent
    data class CategorySelected(val id: String?) : MenuIntent
}

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repository: BrewkeryRepository,
    private val sessionRepository: SessionRepository,
    private val calculateTotals: CalculateCartTotalsUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MenuUiState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    init {
        viewModelScope.launch { sessionRepository.state.collect { session ->
            mutableState.update { it.copy(session = session, cartSubtotal = calculateTotals(session.cart, session.store).subtotal) }
        } }
        loadMenu()
    }
    fun accept(intent: MenuIntent) {
        when (intent) {
            MenuIntent.Retry -> loadMenu()
            is MenuIntent.SearchChanged -> mutableState.update { it.copy(searchQuery = intent.query) }
            is MenuIntent.CategorySelected -> mutableState.update { it.copy(selectedCategoryId = intent.id) }
        }
    }
    private fun loadMenu() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, hasError = false) }
            try {
                val catalog = repository.getMenu()
                sessionRepository.setStore(catalog.store)
                mutableState.update { it.copy(isLoading = false, catalog = catalog) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutableState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }
}
