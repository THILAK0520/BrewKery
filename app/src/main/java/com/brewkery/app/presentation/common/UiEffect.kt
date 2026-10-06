package com.brewkery.app.presentation.common

import androidx.annotation.StringRes

sealed interface UiEffect {
    data object OpenCart : UiEffect
    data object OpenOrder : UiEffect
    data class Message(@param:StringRes @get:StringRes val resource: Int) : UiEffect
}
