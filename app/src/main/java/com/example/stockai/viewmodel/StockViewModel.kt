package com.example.stockai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockai.data.NseStock
import com.example.stockai.data.NseStocks
import com.example.stockai.data.models.*
import com.example.stockai.data.repository.ApiResult
import com.example.stockai.data.repository.StockRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockViewModel : ViewModel() {

    private val repository = StockRepository()

    // ── API results ───────────────────────────────────────
    private val _prediction = MutableStateFlow<ApiResult<PredictionResponse>?>(null)
    val prediction: StateFlow<ApiResult<PredictionResponse>?> = _prediction

    private val _livePrice = MutableStateFlow<ApiResult<LivePriceResponse>?>(null)
    val livePrice: StateFlow<ApiResult<LivePriceResponse>?> = _livePrice

    private val _chartData = MutableStateFlow<ApiResult<ChartResponse>?>(null)
    val chartData: StateFlow<ApiResult<ChartResponse>?> = _chartData

    private val _movers = MutableStateFlow<ApiResult<MoversResponse>?>(null)
    val movers: StateFlow<ApiResult<MoversResponse>?> = _movers

    private val _horizonPrediction =
        MutableStateFlow<ApiResult<HorizonPredictionResponse>?>(null)
    val horizonPrediction: StateFlow<ApiResult<HorizonPredictionResponse>?> =
        _horizonPrediction

    // ── App state ─────────────────────────────────────────
    private val _currentTicker = MutableStateFlow("RELIANCE")
    val currentTicker: StateFlow<String> = _currentTicker

    private val _selectedPeriod = MutableStateFlow("1M")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _watchlist = MutableStateFlow<Set<String>>(
        setOf("RELIANCE", "TCS", "INFY", "HDFCBANK", "SBIN")
    )
    val watchlist: StateFlow<Set<String>> = _watchlist

    private val _suggestions =
        MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val suggestions: StateFlow<List<Pair<String, String>>> = _suggestions

    private val _targetPrediction =
        MutableStateFlow<ApiResult<TargetPredictionResponse>?>(null)
    val targetPrediction: StateFlow<ApiResult<TargetPredictionResponse>?> =
        _targetPrediction

    // ── Local cache ───────────────────────────────────────
    private val localCache =
        mutableMapOf<String,
                Triple<PredictionResponse, LivePriceResponse, ChartResponse?>>()

    // ── Analyze stock ─────────────────────────────────────
    fun analyzeStock(ticker: String) {
        val clean = ticker.uppercase().trim()
        _currentTicker.value = clean
        _suggestions.value   = emptyList()

        viewModelScope.launch {
            // Show cached result instantly if available
            val cached = localCache[clean]
            if (cached != null) {
                _prediction.value = ApiResult.Success(cached.first)
                _livePrice.value  = ApiResult.Success(cached.second)
                if (cached.third != null) {
                    _chartData.value = ApiResult.Success(cached.third!!)
                }
            } else {
                _prediction.value = ApiResult.Loading()
                _livePrice.value  = ApiResult.Loading()
                _chartData.value  = ApiResult.Loading()
            }

            // Fetch fresh in parallel
            val predJob  = async { repository.getPrediction(clean) }
            val priceJob = async { repository.getLivePrice(clean) }
            val chartJob = async {
                repository.getChartData(clean, _selectedPeriod.value)
            }

            val pred  = predJob.await()
            val price = priceJob.await()
            val chart = chartJob.await()

            _prediction.value = pred
            _livePrice.value  = price
            _chartData.value  = chart

            // Save to local cache
            if (pred is ApiResult.Success && price is ApiResult.Success) {
                localCache[clean] = Triple(
                    pred.data,
                    price.data,
                    (chart as? ApiResult.Success)?.data
                )
            }
        }
    }

    suspend fun fetchLivePriceForHorizon(
        ticker: String
    ): Pair<Double, Double>? {
        return try {
            val result = repository.getLivePrice(ticker)
            if (result is ApiResult.Success) {
                Pair(result.data.price, result.data.changePct)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    // ── Chart period change ───────────────────────────────
    fun loadChartData(ticker: String, period: String) {
        _selectedPeriod.value = period
        viewModelScope.launch {
            _chartData.value = ApiResult.Loading()
            _chartData.value = repository.getChartData(ticker, period)
        }
    }

    // ── Market movers ─────────────────────────────────────
    fun loadMarketMovers() {
        viewModelScope.launch {
            _movers.value = ApiResult.Loading()
            _movers.value = repository.getMarketMovers()
        }
    }

    // ── Refresh all ───────────────────────────────────────
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            val predJob   = async { repository.getPrediction(_currentTicker.value) }
            val priceJob  = async { repository.getLivePrice(_currentTicker.value) }
            val chartJob  = async {
                repository.getChartData(_currentTicker.value, _selectedPeriod.value)
            }
            val moversJob = async { repository.getMarketMovers() }

            _prediction.value   = predJob.await()
            _livePrice.value    = priceJob.await()
            _chartData.value    = chartJob.await()
            _movers.value       = moversJob.await()
            _isRefreshing.value = false
        }
    }

    // ── Horizon prediction ────────────────────────────────
    fun getHorizonPrediction(ticker: String, days: Int, strategy: String) {
        viewModelScope.launch {
            _horizonPrediction.value = ApiResult.Loading()
            _horizonPrediction.value =
                repository.getHorizonPrediction(ticker, days, strategy)
        }
    }

    fun scheduleHorizonAlert(
        prediction: HorizonPredictionResponse,
        context:    android.content.Context
    ) {
        com.example.stockai.utils.AlertScheduler
            .scheduleAlert(context, prediction)
    }

    // ── Watchlist ─────────────────────────────────────────
    fun addToWatchlist(ticker: String) {
        _watchlist.value = _watchlist.value + ticker.uppercase()
    }

    fun removeFromWatchlist(ticker: String) {
        _watchlist.value = _watchlist.value - ticker.uppercase()
    }

    fun isInWatchlist(ticker: String): Boolean =
        _watchlist.value.contains(ticker.uppercase())

    // ── Search — uses NseStocks master file ───────────────
    fun updateSuggestions(query: String) {
        if (query.isBlank()) {
            _suggestions.value = emptyList()
            return
        }
        val results: List<NseStock> = NseStocks.search(query, limit = 8)
        _suggestions.value = results.map { stock: NseStock ->
            stock.ticker to stock.name
        }
    }

    fun getTargetPrediction(ticker: String, targetPrice: Double, strategy: String) {
        viewModelScope.launch {
            _targetPrediction.value = ApiResult.Loading()
            _targetPrediction.value = repository.getTargetPrediction(
                ticker, targetPrice, strategy
            )
        }
    }

    // ── Paywall ───────────────────────────────────────────
    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall

    fun showPaywall()  { _showPaywall.value = true  }
    fun hidePaywall()  { _showPaywall.value = false }
    fun checkPremiumStatus() {}

    // ── Init ──────────────────────────────────────────────
    init {
        analyzeStock("RELIANCE")
        loadMarketMovers()

        // Pre-fetch top stocks silently
        viewModelScope.launch {
            delay(5000)
            listOf("TCS", "INFY", "HDFCBANK").forEach { ticker ->
                try {
                    val pred  = repository.getPrediction(ticker)
                    val price = repository.getLivePrice(ticker)
                    val chart = repository.getChartData(ticker, "1M")
                    if (pred  is ApiResult.Success &&
                        price is ApiResult.Success) {
                        localCache[ticker] = Triple(
                            pred.data,
                            price.data,
                            (chart as? ApiResult.Success)?.data
                        )
                    }
                } catch (_: Exception) {}
                delay(2000)
            }
        }
    }
}