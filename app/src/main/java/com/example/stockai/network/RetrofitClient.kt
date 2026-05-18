package com.example.stockai.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Change this to your server URL when deployed
    private const val BASE_URL =
        "https://rohitSanger-tradadvise-backend.hf.space/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC  // BASIC not BODY — faster
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)   // fail fast if server down
        .readTimeout(30, TimeUnit.SECONDS)      // wait 30s for response
        .writeTimeout(10, TimeUnit.SECONDS)
        // Retry once on failure
        .addInterceptor { chain ->
            val request = chain.request()
            try {
                chain.proceed(request)
            } catch (e: Exception) {
                chain.proceed(request)  // retry once
            }
        }
        .build()

    val service: StockApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StockApiService::class.java)
    }
}