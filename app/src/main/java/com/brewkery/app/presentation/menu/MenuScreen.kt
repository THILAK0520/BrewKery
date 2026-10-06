package com.brewkery.app.presentation.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.brewkery.app.R
import com.brewkery.app.domain.model.MenuItem
import com.brewkery.app.presentation.components.*
import com.brewkery.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(state: MenuUiState, onIntent: (MenuIntent) -> Unit, onItem: (Int) -> Unit, onCart: () -> Unit, onOrder: () -> Unit) {
    Scaffold(containerColor = Cream, bottomBar = {
        val currency = state.catalog?.store?.currency
        if (state.session.quantity > 0 && currency != null) {
            Surface(onCart, Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), color = Espresso, shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Terracotta) {
                        Text(state.session.quantity.toString(), Modifier.padding(9.dp), color = Color.White)
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(stringResource(R.string.view_cart), color = WarmBorder, style = MaterialTheme.typography.bodySmall)
                        Text(state.cartSubtotal.format(currency), color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                    Text(stringResource(R.string.checkout), Modifier.widthIn(max = 108.dp), color = Amber, style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Amber)
                }
            }
        }
    }) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.hasError -> ErrorState(R.string.load_error, { onIntent(MenuIntent.Retry) }, Modifier.padding(padding))
            else -> {
                val catalog = state.catalog ?: return@Scaffold
                LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Espresso) { Text("BK", Modifier.padding(12.dp), color = Color.White, style = MaterialTheme.typography.labelLarge) }
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(stringResource(R.string.brand_tagline), color = MutedBrown, style = MaterialTheme.typography.bodySmall)
                                Text(stringResource(R.string.menu_title), style = MaterialTheme.typography.titleLarge)
                            }
                            IconButton(onCart) {
                                BadgedBox(badge = { if (state.session.quantity > 0) Badge { Text(state.session.quantity.toString()) } }) {
                                    Icon(painterResource(R.drawable.ic_shopping_bag), stringResource(R.string.view_cart))
                                }
                            }
                        }
                    }
                    item {
                        val order = state.session.activeOrder
                        Surface(shape = RoundedCornerShape(16.dp), color = SelectionCream, border = BorderStroke(1.dp, WarmBorder),
                            modifier = Modifier.fillMaxWidth().then(if (order != null) Modifier.clickable(onClick = onOrder) else Modifier)) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (order == null) "🛵" else "☕", style = MaterialTheme.typography.headlineSmall)
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(stringResource(if (order == null) R.string.store_info else R.string.active_order), color = Terracotta, style = MaterialTheme.typography.labelSmall)
                                    Text(if (order == null) stringResource(R.string.delivery_time, catalog.store.estimatedDeliveryTime) else order.ticketId,
                                        style = MaterialTheme.typography.labelLarge)
                                    Text(if (order == null) stringResource(R.string.flat_fee, catalog.store.deliveryFee.format(catalog.store.currency))
                                        else stringResource(R.string.track_order), color = MutedBrown, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(stringResource(if (order == null) R.string.store_open else R.string.preparing), color = SuccessGreen, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    item {
                        OutlinedTextField(state.searchQuery, { onIntent(MenuIntent.SearchChanged(it)) }, Modifier.fillMaxWidth(),
                            singleLine = true, shape = RoundedCornerShape(14.dp), placeholder = { Text(stringResource(R.string.search_menu)) },
                            leadingIcon = { Icon(Icons.Outlined.Search, null) })
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { FilterChip(state.selectedCategoryId == null, { onIntent(MenuIntent.CategorySelected(null)) },
                                shape = RoundedCornerShape(50), colors = categoryColors(), label = { Text(stringResource(R.string.all_items)) }) }
                            items(catalog.categories, key = { it.id }) { category ->
                                FilterChip(state.selectedCategoryId == category.id, { onIntent(MenuIntent.CategorySelected(category.id)) },
                                    shape = RoundedCornerShape(50), colors = categoryColors(), label = { Text("${category.icon} ${category.name}") })
                            }
                        }
                    }
                    if (state.visibleItems.isEmpty()) item { Text(stringResource(R.string.no_results), color = MutedBrown) }
                    items(state.visibleItems, key = { it.id }) { item -> MenuProductCard(item, catalog.store.currency) { onItem(item.id) } }
                }
            }
        }
    }
}

@Composable
private fun MenuProductCard(item: MenuItem, currency: String, onClick: () -> Unit) {
    Surface(onClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, WarmBorder)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(item.imageUrl, item.name, Modifier.size(56.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                ProductBadge(item.badge)
                Text(item.name, style = MaterialTheme.typography.labelLarge)
                Text("⭐ ${item.rating} (${item.reviewCount})", color = MutedBrown, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.basePrice.format(currency), Modifier.weight(1f), color = Terracotta, style = MaterialTheme.typography.labelLarge)
                    Button(onClick, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(stringResource(R.string.customize), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun categoryColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color.White, labelColor = MutedBrown,
    selectedContainerColor = Terracotta, selectedLabelColor = Color.White,
)
