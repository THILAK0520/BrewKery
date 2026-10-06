package com.brewkery.app.presentation.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.brewkery.app.R
import com.brewkery.app.presentation.components.*
import com.brewkery.app.presentation.theme.*

@Composable
fun CartScreen(state: CartUiState, onIntent: (CartIntent) -> Unit, onBack: () -> Unit) {
    val currency = state.session.store?.currency
    Scaffold(containerColor = Cream, topBar = {
        Box(Modifier.statusBarsPadding().padding(horizontal = 16.dp)) {
            ScreenHeader(stringResource(R.string.your_cart), onBack) {
                TextButton({ onIntent(CartIntent.Clear) }, enabled = state.session.cart.isNotEmpty() && !state.isPlacingOrder) {
                    Text(stringResource(R.string.clear_cart), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }, bottomBar = {
        if (currency != null) {
            PrimaryAction(stringResource(R.string.place_order, state.totals.total.format(currency)),
                { onIntent(CartIntent.PlaceOrder) }, Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                enabled = state.session.cart.isNotEmpty() && !state.isPlacingOrder)
        }
    }) { padding ->
        if (state.session.cart.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(R.drawable.ic_shopping_bag), null, Modifier.size(56.dp), tint = WarmBorder)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.cart_empty), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.cart_empty_hint), color = MutedBrown, style = MaterialTheme.typography.bodyMedium)
                TextButton(onBack) { Text(stringResource(R.string.back_to_menu)) }
            }
        } else if (currency != null) {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.session.cart, key = { it.lineId }) { line ->
                    Surface(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, WarmBorder)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(line.name, style = MaterialTheme.typography.titleMedium)
                            Text(listOfNotNull(line.sizeLabel, line.milkLabel, line.selection.sugarLevel).joinToString(" • "),
                                color = MutedBrown, style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text((line.unitPrice * line.quantity).format(currency), Modifier.weight(1f), color = Terracotta, style = MaterialTheme.typography.titleMedium)
                                QuantityStepper(line.quantity, { onIntent(CartIntent.ChangeQuantity(line.lineId, -1)) },
                                    { onIntent(CartIntent.ChangeQuantity(line.lineId, 1)) }, minimum = 0, enabled = !state.isPlacingOrder)
                            }
                        }
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, WarmBorder)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            PriceSummaryRow(stringResource(R.string.subtotal), state.totals.subtotal.format(currency))
                            PriceSummaryRow(stringResource(R.string.delivery_fee), state.totals.delivery.format(currency))
                            PriceSummaryRow(stringResource(R.string.estimated_tax, state.session.store?.taxRatePercent?.stripTrailingZeros()?.toPlainString().orEmpty()), state.totals.tax.format(currency))
                            HorizontalDivider(color = WarmBorder)
                            PriceSummaryRow(stringResource(R.string.total_payable), state.totals.total.format(currency), emphasized = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceSummaryRow(label: String, value: String, emphasized: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, Modifier.weight(1f), color = if (emphasized) Espresso else MutedBrown,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
        Text(value, style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge)
    }
}
