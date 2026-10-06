package com.brewkery.app.presentation.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brewkery.app.R
import com.brewkery.app.presentation.components.PrimaryAction
import com.brewkery.app.presentation.theme.*

@Composable
fun OrderStatusScreen(state: OrderStatusUiState, onMenu: () -> Unit) {
    Scaffold(containerColor = Cream, bottomBar = {
        PrimaryAction(stringResource(R.string.back_to_menu), onMenu, Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp))
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(8.dp))
            Surface(shape = CircleShape, color = SelectionCream, border = BorderStroke(1.dp, WarmBorder)) {
                Icon(painterResource(R.drawable.ic_coffee), null, Modifier.padding(20.dp).size(40.dp), tint = Terracotta)
            }
            val order = state.order
            if (order == null) {
                Text(stringResource(R.string.no_active_order), style = MaterialTheme.typography.titleLarge)
            } else {
                Text(stringResource(R.string.order_dispatched), color = Terracotta, style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.brewing_progress), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(stringResource(R.string.barista_ticket), color = MutedBrown, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Surface(shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, WarmBorder), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.order_ticket), color = MutedBrown, style = MaterialTheme.typography.labelSmall)
                        Text(order.ticketId, style = MaterialTheme.typography.headlineLarge)
                        Surface(color = SelectionCream, shape = RoundedCornerShape(50)) {
                            Text(stringResource(R.string.preparing), Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Terracotta, style = MaterialTheme.typography.labelLarge)
                        }
                        HorizontalDivider(color = WarmBorder)
                        OrderSummaryRow(stringResource(R.string.estimated_wait), order.estimatedWait)
                        OrderSummaryRow(stringResource(R.string.items_ordered), pluralStringResource(R.plurals.item_count, order.quantity, order.quantity))
                        OrderSummaryRow(stringResource(R.string.status), stringResource(R.string.barista_accepted))
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, Modifier.weight(1f), color = MutedBrown, style = MaterialTheme.typography.bodySmall)
        Text(value, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.End)
    }
}
