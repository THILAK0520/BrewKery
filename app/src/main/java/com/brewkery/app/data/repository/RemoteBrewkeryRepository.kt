package com.brewkery.app.data.repository

import com.brewkery.app.data.remote.BrewkeryApi
import com.brewkery.app.data.remote.toDomain
import com.brewkery.app.domain.repository.BrewkeryRepository
import javax.inject.Inject

class RemoteBrewkeryRepository @Inject constructor(private val api: BrewkeryApi) : BrewkeryRepository {
    override suspend fun getMenu() = api.getMenu().toDomain()
    override suspend fun getItem(id: Int) = api.getItem(id).toDomain().also { require(it.id == id) }
}
