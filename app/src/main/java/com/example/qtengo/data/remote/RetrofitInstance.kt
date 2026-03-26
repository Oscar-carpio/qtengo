package com.example.qtengo.data.remote

import com.example.qtengo.data.remote.api.ProductApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://TU_API/"

    // 🔹 Instancia única de Retrofit
    val api: ProductApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApi::class.java)
    }
}