package com.brewkery.app.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.brewkery.app.R
import com.brewkery.app.presentation.theme.*

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, action: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, border = BorderStroke(1.dp, WarmBorder)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
        }
        Text(title, Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.labelLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Box(Modifier.widthIn(min = 48.dp), contentAlignment = Alignment.Center) { action() }
    }
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick, modifier.heightIn(min = 52.dp), enabled = enabled,
        shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun QuantityStepper(quantity: Int, onDecrease: () -> Unit, onIncrease: () -> Unit, minimum: Int = 1, enabled: Boolean = true) {
    Surface(shape = RoundedCornerShape(12.dp), color = SelectionCream, border = BorderStroke(1.dp, WarmBorder)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onDecrease, enabled = enabled && quantity > minimum) {
                Icon(painterResource(R.drawable.ic_remove), stringResource(R.string.decrease_quantity), Modifier.size(18.dp))
            }
            Text(quantity.toString(), style = MaterialTheme.typography.labelLarge)
            IconButton(onIncrease, enabled = enabled && quantity < 99) {
                Icon(Icons.Default.Add, stringResource(R.string.increase_quantity), Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ProductImage(url: String?, description: String, modifier: Modifier = Modifier) {
    // Coil decodes at the constrained dimensions; the image URL stays exactly as supplied by the API.
    SubcomposeAsyncImage(model = url, contentDescription = description,
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(SelectionCream),
        contentScale = ContentScale.Crop,
        loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        } },
        error = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Warning, stringResource(R.string.image_unavailable), tint = MutedBrown)
        } })
}

@Composable
fun ProductBadge(text: String, modifier: Modifier = Modifier, onImage: Boolean = false) {
    if (text.isNotBlank()) Surface(modifier, shape = RoundedCornerShape(6.dp), color = if (onImage) Espresso else Color(0xFFFEF3C7)) {
        Text(text, Modifier.padding(horizontal = 6.dp, vertical = 4.dp), color = if (onImage) Amber else Color(0xFF78350F), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun OptionSelector(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extra: String? = null,
    inlinePrice: Boolean = false,
    darkSelection: Boolean = false,
) {
    val selectedColor = if (darkSelection) Espresso else SelectionCream
    val labelColor = if (selected && darkSelection) Color.White else if (selected) Terracotta else MutedBrown
    Surface(onClick, modifier.heightIn(min = 48.dp).semantics { this.selected = selected; role = Role.RadioButton }, shape = RoundedCornerShape(10.dp),
        color = if (selected) selectedColor else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) Terracotta else WarmBorder)) {
        if (inlinePrice) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(label, Modifier.weight(1f), color = labelColor, style = MaterialTheme.typography.labelLarge)
                extra?.let { Text(it, color = labelColor, style = MaterialTheme.typography.bodySmall) }
            }
        } else {
            Column(Modifier.padding(12.dp)) {
                Text(label, color = labelColor, style = MaterialTheme.typography.labelLarge)
                extra?.let { Text(it, color = MutedBrown, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.loading)
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.semantics { contentDescription = description })
    }
}

@Composable
fun ErrorState(@StringRes message: Int, retry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(message), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        PrimaryAction(stringResource(R.string.retry), retry)
    }
}
