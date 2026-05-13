package com.example.stockai.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /*
     * BASE URL RULES:
     * ─────────────────────────────────────────────────
     * Android Emulator → use 10.0.2.2 (maps to your PC localhost)
     * Real Android device on same WiFi → use your PC's local IP
     *   Find it: Windows → cmd → ipconfig → IPv4 Address
     *   Example: http://192.168.1.5:8000/
     * Production → use your Render.com URL
     *   Example: https://stockai-backend.onrender.com/
     */
    private const val BASE_URL = "http://192.168.31.176:8000/"

    // Shows all HTTP requests/responses in Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)  // wait 30s to connect
        .readTimeout(60, TimeUnit.SECONDS)     // wait 60s for response
        // prediction can take time on first call
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: StockApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StockApiService::class.java)
    }
}