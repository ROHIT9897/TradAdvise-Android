package com.example.stockai.network

import com.example.stockai.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface StockApiService {

    // Full AI prediction for a stock
    @GET("api/v1/analyze/{ticker}")
    suspend fun analyzeTicker(
        @Path("ticker") ticker: String
    ): Response<PredictionResponse>

    // Live price
    @GET("api/v1/price/{ticker}")
    suspend fun getLivePrice(
        @Path("ticker") ticker: String
    ): Response<LivePriceResponse>

    // News + sentiment
    @GET("api/v1/news/{ticker}")
    suspend fun getNews(
        @Path("ticker") ticker: String
    ): Response<NewsResponse>

    // Top gainers and losers
    @GET("api/v1/market/movers")
    suspend fun getMarketMovers(): Response<MoversResponse>

    // Train model for a ticker
    @POST("api/v1/train/{ticker}")
    suspend fun trainModel(
        @Path("ticker") ticker: String
    ): Response<Map<String, Any>>

    // Health check
    @GET("api/v1/health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/v1/chart/{ticker}")
    suspend fun getChartData(
        @Path("ticker")  ticker: String,
        @Query("period") period: String = "1M"
    ): Response<ChartResponse>

    @GET("api/v1/predict/{ticker}")
    suspend fun getHorizonPrediction(
        @Path("ticker")    ticker:   String,
        @Query("days")     days:     Int,
        @Query("strategy") strategy: String = "hold"
    ): Response<HorizonPredictionResponse>    // ← Response<> wrapper add karo

}