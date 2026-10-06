package com.brewkery.app.data

import com.brewkery.app.data.remote.BrewkeryApi
import com.brewkery.app.data.repository.RemoteBrewkeryRepository
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RemoteRepositoryTest {
    @Test fun `menu uses GitHub JSON field names with no fixed item count`() = runTest {
        val server = MockWebServer()
        try {
            server.enqueue(MockResponse().setBody("""{
              "meta":{"app":"Remote Store","currency":"USD","delivery_fee":2.5,"tax_rate_percent":8,"estimated_delivery_time":"25 mins"},
              "categories":[{"id":"remote","name":"Remote category","icon":""}],
              "items":[{"id":37,"category_id":"remote","name":"Remote product","description":"From server","base_price":7.15,"rating":4.8,"review_count":7,"image_url":"https://images.unsplash.com/photo-test","ingredients":["Remote ingredient"],"customizations":{"sizes":[{"id":"size","label":"Server size","extra_price":0.65}],"milk_options":[],"sugar_levels":[]}}]
            }"""))
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(BrewkeryApi::class.java)
            val catalog = RemoteBrewkeryRepository(api).getMenu()
            assertEquals(1, catalog.items.size)
            assertEquals(37, catalog.items.single().id)
            assertEquals("Remote product", catalog.items.single().name)
            assertEquals(715L, catalog.items.single().basePrice.cents)
            assertEquals(65L, catalog.items.single().customizations.sizes.single().extraPrice.cents)
            assertEquals("/data.json", server.takeRequest().path)
        } finally { server.shutdown() }
    }
}
