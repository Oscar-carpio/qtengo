package com.example.qtengo.data.remote.api

import com.example.qtengo.data.local.model.Product
import retrofit2.http.*

interface ProductApi {

    // 🔹 Obtener productos desde servidor
    @GET("products")
    suspend fun getProducts(@Query("profile") profile: String): List<Product>

    // 🔹 Insertar producto en servidor
    @POST("products")
    suspend fun insertProduct(@Body product: Product)

    // 🔹 Actualizar producto
    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body product: Product
    )

    // 🔹 Eliminar producto
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int)
}