package com.example.qtengo.core.data.network.api

import com.example.qtengo.core.domain.models.Product
import retrofit2.http.*

interface ProductApi {
    @GET("products")
    suspend fun getProducts(@Query("profile") profile: String): List<Product>

    @POST("products")
    suspend fun insertProduct(@Body product: Product)

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: Product)

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int)
}