package com.brewkery.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface BrewkeryApi {
    @GET("data.json") suspend fun getMenu(): MenuDto
    @GET("api/items/{id}.json") suspend fun getItem(@Path("id") id: Int): MenuItemDto
}
