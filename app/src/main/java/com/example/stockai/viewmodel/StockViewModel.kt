package com.example.stockai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockai.data.models.*
import com.example.stockai.data.repository.ApiResult
import com.example.stockai.data.repository.StockRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockViewModel : ViewModel() {

    private val repository = StockRepository()

    private val _prediction = MutableStateFlow<ApiResult<PredictionResponse>?>(null)
    val prediction: StateFlow<ApiResult<PredictionResponse>?> = _prediction

    private val _livePrice = MutableStateFlow<ApiResult<LivePriceResponse>?>(null)
    val livePrice: StateFlow<ApiResult<LivePriceResponse>?> = _livePrice

    private val _movers = MutableStateFlow<ApiResult<MoversResponse>?>(null)
    val movers: StateFlow<ApiResult<MoversResponse>?> = _movers

    // ── Chart data — was missing ──────────────────────────
    private val _chartData = MutableStateFlow<ApiResult<ChartResponse>?>(null)
    val chartData: StateFlow<ApiResult<ChartResponse>?> = _chartData

    private val _selectedPeriod = MutableStateFlow("1M")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    private val _currentTicker = MutableStateFlow("RELIANCE")
    val currentTicker: StateFlow<String> = _currentTicker

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _watchlist = MutableStateFlow<Set<String>>(
        setOf("RELIANCE", "TCS", "INFY", "HDFCBANK", "SBIN")
    )
    val watchlist: StateFlow<Set<String>> = _watchlist

    private val _suggestions = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val suggestions: StateFlow<List<Pair<String, String>>> = _suggestions

    // ── Analyze stock ─────────────────────────────────────
    fun analyzeStock(ticker: String) {
        val clean = ticker.uppercase().trim()
        _currentTicker.value = clean
        _suggestions.value   = emptyList()

        viewModelScope.launch {
            _prediction.value = ApiResult.Loading()
            _livePrice.value  = ApiResult.Loading()
            _chartData.value  = ApiResult.Loading()

            val predJob  = async { repository.getPrediction(clean) }
            val priceJob = async { repository.getLivePrice(clean) }
            val chartJob = async { repository.getChartData(clean, _selectedPeriod.value) }

            _prediction.value = predJob.await()
            _livePrice.value  = priceJob.await()
            _chartData.value  = chartJob.await()
        }
    }

    // ── Load chart for specific period ────────────────────
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
            val chartJob  = async { repository.getChartData(_currentTicker.value, _selectedPeriod.value) }
            val moversJob = async { repository.getMarketMovers() }

            _prediction.value   = predJob.await()
            _livePrice.value    = priceJob.await()
            _chartData.value    = chartJob.await()
            _movers.value       = moversJob.await()
            _isRefreshing.value = false
        }
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

    // ── Search suggestions ────────────────────────────────
    fun updateSuggestions(query: String) {
        if (query.isBlank()) {
            _suggestions.value = emptyList()
            return
        }
        val q = query.uppercase()
        _suggestions.value = ALL_STOCKS
            .filter { (ticker, name) ->
                ticker.startsWith(q) || name.uppercase().contains(q)
            }
            .take(6)
    }

    init {
        analyzeStock("RELIANCE")
        loadMarketMovers()
    }
}

// ── Stock universe for search suggestions ─────────────────
val ALL_STOCKS = listOf(
    "RELIANCE"   to "Reliance Industries",
    "TCS"        to "Tata Consultancy Services",
    "INFY"       to "Infosys",
    "HDFCBANK"   to "HDFC Bank",
    "ICICIBANK"  to "ICICI Bank",
    "SBIN"       to "State Bank of India",
    "BHARTIARTL" to "Bharti Airtel",
    "WIPRO"      to "Wipro",
    "LT"         to "Larsen and Toubro",
    "AXISBANK"   to "Axis Bank",
    "KOTAKBANK"  to "Kotak Mahindra Bank",
    "HINDUNILVR" to "Hindustan Unilever",
    "ITC"        to "ITC Limited",
    "SUNPHARMA"  to "Sun Pharmaceutical",
    "MARUTI"     to "Maruti Suzuki",
    "BAJFINANCE" to "Bajaj Finance",
    "TITAN"      to "Titan Company",
    "ASIANPAINT" to "Asian Paints",
    "NESTLEIND"  to "Nestle India",
    "PNB"        to "Punjab National Bank",
    "ADANIENT"   to "Adani Enterprises",
    "ADANIPORTS" to "Adani Ports",
    "POWERGRID"  to "Power Grid Corporation",
    "NTPC"       to "NTPC Limited",
    "ONGC"       to "Oil and Natural Gas",
    "COALINDIA"  to "Coal India",
    "BPCL"       to "Bharat Petroleum",
    "HCLTECH"    to "HCL Technologies",
    "TECHM"      to "Tech Mahindra",
    "DRREDDY"    to "Dr Reddy Laboratories",
    "CIPLA"      to "Cipla",
    "BAJAJFINSV" to "Bajaj Finserv",
    "TATAMOTORS" to "Tata Motors",
    "TATASTEEL"  to "Tata Steel",
    "JSWSTEEL"   to "JSW Steel",
    "HINDALCO"   to "Hindalco Industries",
    "ULTRACEMCO" to "UltraTech Cement",
    "EICHERMOT"  to "Eicher Motors",
    "HEROMOTOCO" to "Hero MotoCorp",
    "ZOMATO"     to "Zomato",
    "IRCTC"      to "Indian Railway Catering",
    "TATAPOWER"  to "Tata Power",
    "HAL"        to "Hindustan Aeronautics",
    "BEL"        to "Bharat Electronics",
)