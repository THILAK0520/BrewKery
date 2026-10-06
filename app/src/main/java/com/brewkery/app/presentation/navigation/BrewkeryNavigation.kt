package com.brewkery.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.brewkery.app.presentation.cart.*
import com.brewkery.app.presentation.common.UiEffect
import com.brewkery.app.presentation.detail.*
import com.brewkery.app.presentation.menu.*
import com.brewkery.app.presentation.order.*
import kotlinx.coroutines.flow.Flow

@Composable
fun BrewkeryNavigation() {
    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val handleEffect: suspend (UiEffect) -> Unit = { effect ->
        when (effect) {
            UiEffect.OpenCart -> navController.navigate("cart") { popUpTo("menu"); launchSingleTop = true }
            UiEffect.OpenOrder -> navController.navigate("order") { popUpTo("menu"); launchSingleTop = true }
            is UiEffect.Message -> snackbar.showSnackbar(context.getString(effect.resource))
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        NavHost(navController, startDestination = "menu", modifier = Modifier.widthIn(max = 600.dp).fillMaxSize()) {
            composable("menu") {
                val viewModel: MenuViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                MenuScreen(state, viewModel::accept, { navController.navigate("detail/$it") },
                    { navController.navigate("cart") { launchSingleTop = true } },
                    { navController.navigate("order") { launchSingleTop = true } })
            }
            composable("detail/{itemId}", arguments = listOf(navArgument("itemId") { type = NavType.IntType })) {
                val viewModel: ItemDetailViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                CollectEffects(viewModel.effects, handleEffect)
                ItemDetailScreen(state, viewModel::accept) { navController.popBackStack() }
            }
            composable("cart") {
                val viewModel: CartViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                CollectEffects(viewModel.effects, handleEffect)
                CartScreen(state, viewModel::accept) { navController.popBackStack("menu", false) }
            }
            composable("order") {
                val viewModel: OrderStatusViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                OrderStatusScreen(state) { navController.popBackStack("menu", false) }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun CollectEffects(effects: Flow<UiEffect>, onEffect: suspend (UiEffect) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentHandler by rememberUpdatedState(onEffect)
    LaunchedEffect(effects, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { effects.collect { currentHandler(it) } }
    }
}
