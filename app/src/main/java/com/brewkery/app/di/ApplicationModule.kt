package com.brewkery.app.di

import com.brewkery.app.data.remote.BrewkeryApi
import com.brewkery.app.data.repository.RemoteBrewkeryRepository
import com.brewkery.app.data.session.InMemorySessionRepository
import com.brewkery.app.domain.repository.BrewkeryRepository
import com.brewkery.app.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindMenu(repository: RemoteBrewkeryRepository): BrewkeryRepository
    @Binds @Singleton abstract fun bindSession(repository: InMemorySessionRepository): SessionRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS).followSslRedirects(false).build()

    @Provides @Singleton fun provideApi(client: OkHttpClient): BrewkeryApi = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/VivekShah138/Brewkery/main/")
        .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(BrewkeryApi::class.java)
}
