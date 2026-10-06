package com.brewkery.app.presentation

import androidx.lifecycle.SavedStateHandle
import com.brewkery.app.data.session.InMemorySessionRepository
import com.brewkery.app.domain.model.*
import com.brewkery.app.domain.repository.BrewkeryRepository
import com.brewkery.app.domain.usecase.*
import com.brewkery.app.presentation.common.UiEffect
import com.brewkery.app.presentation.cart.*
import com.brewkery.app.presentation.detail.*
import com.brewkery.app.presentation.menu.*
import com.brewkery.app.testItem
import com.brewkery.app.testStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val session = InMemorySessionRepository(CalculateCartTotalsUseCase())
    private val repository = FakeRepository()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `menu network failure exposes retry then loads fetched products`() = runTest(dispatcher) {
        repository.fail = true
        val vm = MenuViewModel(repository, session, CalculateCartTotalsUseCase())
        assertTrue(vm.state.value.isLoading)
        runCurrent()
        assertTrue(vm.state.value.hasError)
        assertNull(vm.state.value.catalog)
        repository.fail = false
        vm.accept(MenuIntent.Retry)
        runCurrent()
        assertFalse(vm.state.value.hasError)
        assertEquals(listOf(testItem()), vm.state.value.visibleItems)
        vm.accept(MenuIntent.SearchChanged("missing"))
        assertTrue(vm.state.value.visibleItems.isEmpty())
        vm.accept(MenuIntent.SearchChanged("test"))
        vm.accept(MenuIntent.CategorySelected("other"))
        assertTrue(vm.state.value.visibleItems.isEmpty())
    }
    @Test fun `detail preserves option selections and prevents repeated additions`() = runTest(dispatcher) {
        session.setStore(testStore())
        val price = CalculateItemPriceUseCase()
        val vm = ItemDetailViewModel(SavedStateHandle(mapOf("itemId" to 1)), repository, session, price, AddCartItemUseCase(session, price))
        runCurrent()
        vm.accept(ItemDetailIntent.SelectSize("large"))
        vm.accept(ItemDetailIntent.SelectMilk("extra"))
        vm.accept(ItemDetailIntent.SelectSugar("Unsweetened"))
        vm.accept(ItemDetailIntent.ChangeQuantity(1))
        assertEquals(ItemSelection("large", "extra", "Unsweetened"), vm.state.value.selection)
        assertEquals(Money(1200), vm.state.value.totalPrice)
        vm.accept(ItemDetailIntent.AddToCart)
        vm.accept(ItemDetailIntent.AddToCart)
        runCurrent()
        assertEquals(1, session.state.value.cart.size)
        assertEquals(2, session.state.value.quantity)
        assertEquals(UiEffect.OpenCart, vm.effects.first())
    }
    @Test fun `detail failure is retryable and does not invent an item`() = runTest(dispatcher) {
        repository.fail = true
        val price = CalculateItemPriceUseCase()
        val vm = ItemDetailViewModel(SavedStateHandle(mapOf("itemId" to 1)), repository, session, price, AddCartItemUseCase(session, price))
        runCurrent()
        assertTrue(vm.state.value.hasError)
        assertNull(vm.state.value.item)
        repository.fail = false
        vm.accept(ItemDetailIntent.Retry)
        runCurrent()
        assertEquals(testItem(), vm.state.value.item)
        assertFalse(vm.state.value.hasError)
    }
    @Test fun `checkout emits one navigation effect and disables repeated taps`() = runTest(dispatcher) {
        session.setStore(testStore())
        AddCartItemUseCase(session, CalculateItemPriceUseCase())(testItem(), testItem().defaultSelection(), 1)
        val vm = CartViewModel(session, CalculateCartTotalsUseCase(), PlaceOrderUseCase(session))
        runCurrent()
        vm.accept(CartIntent.PlaceOrder)
        vm.accept(CartIntent.PlaceOrder)
        runCurrent()
        assertTrue(vm.state.value.isPlacingOrder)
        assertTrue(vm.state.value.session.cart.isEmpty())
        assertEquals(1, vm.state.value.session.activeOrder?.quantity)
        assertEquals(UiEffect.OpenOrder, vm.effects.first())
    }

    private class FakeRepository : BrewkeryRepository {
        var fail = false
        override suspend fun getMenu(): MenuCatalog {
            if (fail) error("Network failure")
            return MenuCatalog(testStore(), listOf(MenuCategory("test", "Test", "")), listOf(testItem()))
        }
        override suspend fun getItem(id: Int): MenuItem {
            if (fail) error("Network failure")
            return testItem()
        }
    }
}
