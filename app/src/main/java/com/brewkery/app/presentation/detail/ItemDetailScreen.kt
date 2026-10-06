package com.brewkery.app.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brewkery.app.R
import com.brewkery.app.presentation.components.*
import com.brewkery.app.presentation.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemDetailScreen(state: ItemDetailUiState, onIntent: (ItemDetailIntent) -> Unit, onBack: () -> Unit) {
    Scaffold(containerColor = Cream, topBar = {
        Box(Modifier.statusBarsPadding().padding(horizontal = 16.dp)) {
            ScreenHeader(stringResource(R.string.item_customizer), onBack) {
                IconButton({ onIntent(ItemDetailIntent.ToggleFavorite) }, enabled = state.item != null) {
                    Icon(if (state.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        stringResource(if (state.isFavorite) R.string.unfavorite else R.string.favorite), tint = Color(0xFFE34D70))
                }
            }
        }
    }, bottomBar = {
        val currency = state.currency
        if (state.item != null && !state.hasError && !state.isLoading && currency != null) {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(state.quantity, { onIntent(ItemDetailIntent.ChangeQuantity(-1)) },
                    { onIntent(ItemDetailIntent.ChangeQuantity(1)) }, enabled = !state.isAdding)
                PrimaryAction(stringResource(R.string.add_to_cart, state.totalPrice.format(currency)),
                    { onIntent(ItemDetailIntent.AddToCart) }, Modifier.weight(1f), enabled = !state.isAdding)
            }
        }
    }) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.hasError -> ErrorState(R.string.detail_error, { onIntent(ItemDetailIntent.Retry) }, Modifier.padding(padding))
            else -> {
                val item = state.item ?: return@Scaffold
                val currency = state.currency ?: return@Scaffold
                LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Box {
                            ProductImage(item.imageUrl, item.name, Modifier.fillMaxWidth().aspectRatio(347f / 160f))
                            ProductBadge(item.badge, Modifier.padding(12.dp), onImage = true)
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(item.name, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                            Text(item.basePrice.format(currency), Modifier.padding(start = 8.dp), color = Terracotta, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    item { Text(item.description, color = MutedBrown, style = MaterialTheme.typography.bodyMedium) }
                    if (item.ingredients.isNotEmpty()) item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.key_ingredients), style = MaterialTheme.typography.labelSmall)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.ingredients.forEach { ingredient ->
                                    Surface(color = SelectionCream, shape = RoundedCornerShape(8.dp)) {
                                        Text(ingredient, Modifier.padding(8.dp), color = MutedBrown, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                    if (item.customizations.sizes.isNotEmpty()) item {
                        CustomizationSection(stringResource(R.string.size_selection)) {
                            FlowRow(maxItemsInEachRow = 3, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item.customizations.sizes.forEach { size ->
                                    OptionSelector(size.label, state.selection.sizeId == size.id, { onIntent(ItemDetailIntent.SelectSize(size.id)) },
                                        Modifier.weight(1f), extra = "+${size.extraPrice.format(currency)}")
                                }
                            }
                        }
                    }
                    if (item.customizations.milkOptions.isNotEmpty()) item {
                        CustomizationSection(stringResource(R.string.milk_options)) {
                            item.customizations.milkOptions.forEach { milk ->
                                OptionSelector(milk.label, state.selection.milkOptionId == milk.id, { onIntent(ItemDetailIntent.SelectMilk(milk.id)) },
                                    Modifier.fillMaxWidth(), extra = "+${milk.extraPrice.format(currency)}", inlinePrice = true)
                            }
                        }
                    }
                    if (item.customizations.sugarLevels.isNotEmpty()) item {
                        CustomizationSection(stringResource(R.string.sugar_levels)) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item.customizations.sugarLevels.forEach { sugar ->
                                    OptionSelector(sugar, state.selection.sugarLevel == sugar, { onIntent(ItemDetailIntent.SelectSugar(sugar)) }, darkSelection = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizationSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, WarmBorder), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            content()
        }
    }
}
