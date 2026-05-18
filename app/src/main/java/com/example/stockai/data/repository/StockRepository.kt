// data/repository/StockRepository.kt
package com.example.stockai.data.repository

import com.example.stockai.data.models.*
import com.example.stockai.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String) : ApiResult<T>()
    class Loading<T> : ApiResult<T>()
}

class StockRepository {

    private val api = RetrofitClient.service

    suspend fun getPrediction(ticker: String): ApiResult<PredictionResponse> =
        safeCall { api.analyzeTicker(ticker) }

    suspend fun getLivePrice(ticker: String): ApiResult<LivePriceResponse> =
        safeCall { api.getLivePrice(ticker) }

    suspend fun getMarketMovers(): ApiResult<MoversResponse> =
        safeCall { api.getMarketMovers() }

    suspend fun getChartData(ticker: String, period: String): ApiResult<ChartResponse> =
        safeCall { api.getChartData(ticker, period) }

    private suspend fun <T> safeCall(
        call: suspend () -> retrofit2.Response<T>
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Server error ${response.code()}: ${response.message()}")
            }
        } catch (e: java.net.ConnectException) {
            ApiResult.Error("Cannot connect to server.\nMake sure backend is running.")
        } catch (e: java.net.SocketTimeoutException) {
            ApiResult.Error("Request timed out. Server is busy.")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    suspend fun getHorizonPrediction(
        ticker:   String,
        days:     Int,
        strategy: String
    ): ApiResult<HorizonPredictionResponse> = safeCall {
        api.getHorizonPrediction(ticker, days, strategy)
    }

    suspend fun getTargetPrediction(
        ticker:      String,
        targetPrice: Double,
        strategy:    String
    ): ApiResult<TargetPredictionResponse> = safeCall {
        api.getTargetPrediction(ticker, targetPrice, strategy)
    }

}