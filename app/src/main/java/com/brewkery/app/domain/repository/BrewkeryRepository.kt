package com.brewkery.app.domain.repository

import com.brewkery.app.domain.model.MenuCatalog
import com.brewkery.app.domain.model.MenuItem

interface BrewkeryRepository {
    suspend fun getMenu(): MenuCatalog
    suspend fun getItem(id: Int): MenuItem
}
