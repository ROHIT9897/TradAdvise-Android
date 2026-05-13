// data/models/ApiModels.kt
package com.example.stockai.data.models

import com.google.gson.annotations.SerializedName

// ── /analyze/{ticker} ───────────────────────────────────

data class PredictionResponse(
    @SerializedName("signal")              val signal: String,
    @SerializedName("score")               val score: Double,
    @SerializedName("confidence")          val confidence: Double,
    @SerializedName("agreement")           val agreement: String,
    @SerializedName("bull_indicators")     val bullIndicators: Int,
    @SerializedName("bear_indicators")     val bearIndicators: Int,
    @SerializedName("ml_confirmation")     val mlConfirmation: Double?,
    @SerializedName("validation")          val validation: Validation,
    @SerializedName("sentiment")           val sentiment: String,
    @SerializedName("sentiment_score")     val sentimentScore: Double,
    @SerializedName("signal_note")         val signalNote: String,
    @SerializedName("explanation")         val explanation: List<String>,
    @SerializedName("indicators")          val indicators: RawIndicators,
    @SerializedName("individual_signals")  val individualSignals: IndividualSignals,
    @SerializedName("news")                val news: NewsResponse,
    @SerializedName("cached")              val cached: Boolean = false
)

data class Validation(
    @SerializedName("final_signal")      val finalSignal: String,
    @SerializedName("is_validated")      val isValidated: Boolean,
    @SerializedName("blocked_reasons")   val blockedReasons: List<String>,
    @SerializedName("passed_checks")     val passedChecks: List<String>
)

data class RawIndicators(
    @SerializedName("rsi")         val rsi: Double,
    @SerializedName("bb_position") val bbPosition: Double,
    @SerializedName("vol_ratio")   val volRatio: Double,
    @SerializedName("ma20_slope")  val ma20Slope: Double
)

data class IndividualSignals(
    @SerializedName("trend")    val trend: Double,
    @SerializedName("momentum") val momentum: Double,
    @SerializedName("macd")     val macd: Double,
    @SerializedName("bands")    val bands: Double,
    @SerializedName("volume")   val volume: Double
)

// ── /price/{ticker} ─────────────────────────────────────

data class LivePriceResponse(
    @SerializedName("ticker")          val ticker: String,
    @SerializedName("price")           val price: Double,
    @SerializedName("previous_close")  val previousClose: Double,
    @SerializedName("change")          val change: Double,
    @SerializedName("change_pct")      val changePct: Double,
    @SerializedName("day_high")        val dayHigh: Double,
    @SerializedName("day_low")         val dayLow: Double,
    @SerializedName("volume")          val volume: Long,
    @SerializedName("cached")          val cached: Boolean = false
)

// ── /news/{ticker} ──────────────────────────────────────

data class NewsResponse(
    @SerializedName("overall_sentiment") val overallSentiment: String,
    @SerializedName("sentiment_score")   val sentimentScore: Double,
    @SerializedName("breakdown")         val breakdown: SentimentBreakdown,
    @SerializedName("articles")          val articles: List<NewsArticle>
)

data class SentimentBreakdown(
    @SerializedName("positive") val positive: Int,
    @SerializedName("negative") val negative: Int,
    @SerializedName("neutral")  val neutral: Int
)

data class NewsArticle(
    @SerializedName("title")        val title: String,
    @SerializedName("source")       val source: String,
    @SerializedName("url")          val url: String,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("sentiment")    val sentiment: String,
    @SerializedName("confidence")   val confidence: Map<String, Double>
)

// ── /market/movers ──────────────────────────────────────

data class MoversResponse(
    @SerializedName("top_gainers") val topGainers: List<LivePriceResponse>,
    @SerializedName("top_losers")  val topLosers: List<LivePriceResponse>
)

// ── /health ─────────────────────────────────────────────

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("redis")  val redis: Boolean
)
data class ChartResponse(
    @SerializedName("ticker")  val ticker: String,
    @SerializedName("period")  val period: String,
    @SerializedName("count")   val count: Int,
    @SerializedName("prices")  val prices: List<PricePoint>
)

data class PricePoint(
    @SerializedName("date")   val date: String,
    @SerializedName("open")   val open: Double,
    @SerializedName("high")   val high: Double,
    @SerializedName("low")    val low: Double,
    @SerializedName("close")  val close: Double,
    @SerializedName("volume") val volume: Long
)