package com.example.stockai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.example.stockai.data.models.HorizonPredictionResponse
import com.example.stockai.data.models.TargetPredictionResponse
import com.example.stockai.data.repository.ApiResult
import com.example.stockai.ui.theme.*
import com.example.stockai.utils.AlertScheduler
import com.example.stockai.viewmodel.StockViewModel

@Composable
fun HorizonScreen(
    ticker:    String,
    viewModel: StockViewModel
) {
    val context = LocalContext.current

    var days            by remember { mutableStateOf(30) }
    var strategy        by remember { mutableStateOf("hold") }
    var selectedTicker  by remember { mutableStateOf(ticker) }
    var searchText      by remember { mutableStateOf("") }
    var showDropdown    by remember { mutableStateOf(false) }
    var useCustomTarget by remember { mutableStateOf(false) }
    var targetPriceText by remember { mutableStateOf("") }

    // Live price state for selected stock
    var livePrice     by remember { mutableStateOf<Double?>(null) }
    var livePricePct  by remember { mutableStateOf<Double?>(null) }
    var livePriceLoad by remember { mutableStateOf(false) }

    val prediction       by viewModel.horizonPrediction.collectAsState()
    val targetPrediction by viewModel.targetPrediction.collectAsState()

    val popularStocks = listOf(
        "RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK",
        "SBIN", "BHARTIARTL", "WIPRO", "LT", "AXISBANK",
        "KOTAKBANK", "ITC", "SUNPHARMA", "MARUTI", "BAJFINANCE",
        "TITAN", "ADANIENT", "NESTLEIND", "TATAMOTORS", "ZOMATO",
        "HCLTECH", "TECHM", "DRREDDY", "CIPLA", "BAJAJFINSV",
        "TATASTEEL", "JSWSTEEL", "HINDALCO", "ULTRACEMCO", "PNB",
        "ZYDUSLIFE", "PAYTM", "NYKAA", "IRFC", "RVNL",
        "BEL", "HAL", "IRCTC", "TATAPOWER", "DMART"
    )

    val filteredStocks = if (searchText.isBlank()) popularStocks
    else popularStocks.filter { it.contains(searchText.uppercase()) }

    // Fetch live price when selectedTicker changes
    LaunchedEffect(selectedTicker) {
        livePriceLoad = true
        livePrice     = null
        livePricePct  = null
        try {
            val result = viewModel.fetchLivePriceForHorizon(selectedTicker)
            livePrice    = result?.first
            livePricePct = result?.second
        } catch (_: Exception) {}
        livePriceLoad = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Header ────────────────────────────────────────
        Text(
            "Investment Horizon",
            color      = TextPrimary,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "Predict target price for any custom period",
            color    = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(16.dp))

        // ── Stock selector ────────────────────────────────
        Text("Select Stock", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value         = searchText,
                onValueChange = {
                    searchText   = it
                    showDropdown = true
                },
                placeholder = {
                    Text(
                        "Search or type any NSE ticker...",
                        color    = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AccentBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            selectedTicker,
                            color      = AccentBlue,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(
                                horizontal = 8.dp, vertical = 4.dp
                            )
                        )
                    }
                },
                trailingIcon = {
                    if (searchText.isNotBlank()) {
                        IconButton(onClick = {
                            searchText   = ""
                            showDropdown = false
                        }) {
                            Text("✕", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                },
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(12.dp),
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AccentBlue,
                    unfocusedBorderColor = Color(0xFF1A2535),
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary,
                    cursorColor          = AccentBlue
                )
            )

            // Dropdown
            if (showDropdown && searchText.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                        .zIndex(10f),
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, Color(0xFF1A2535))
                ) {
                    Column {
                        // Use typed ticker directly
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTicker = searchText.uppercase().trim()
                                    searchText     = ""
                                    showDropdown   = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier        = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen.copy(alpha = 0.12f))
                                    .border(1.dp, AccentGreen.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Use \"${searchText.uppercase()}\"",
                                    color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold
                                )
                                Text("Tap to use this ticker directly", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        if (filteredStocks.isNotEmpty()) {
                            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp)
                            filteredStocks.take(5).forEachIndexed { i, stock ->
                                Row(
                                    modifier          = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTicker = stock
                                            searchText     = ""
                                            showDropdown   = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier        = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(AccentBlue.copy(alpha = 0.12f))
                                            .border(1.dp, AccentBlue.copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(stock.take(2), color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(stock, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                                if (i < filteredStocks.take(5).lastIndex) {
                                    HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Live price card ───────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            colors   = CardDefaults.cardColors(containerColor = BgCard),
            border   = BorderStroke(1.dp, Color(0xFF1A2535))
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Stock circle
                    Box(
                        modifier        = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.12f))
                            .border(1.dp, AccentBlue.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            selectedTicker.take(2),
                            color      = AccentBlue,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            selectedTicker,
                            color      = TextPrimary,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "NSE",
                            color    = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Price section
                if (livePriceLoad) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = AccentBlue,
                        strokeWidth = 2.dp
                    )
                } else if (livePrice != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "₹${livePrice}",
                            color      = TextPrimary,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        val pct        = livePricePct ?: 0.0
                        val pctColor   = if (pct >= 0) AccentGreen else AccentRed
                        val pctPrefix  = if (pct >= 0) "▲" else "▼"
                        Text(
                            "$pctPrefix ${String.format("%.2f", pct)}%",
                            color    = pctColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        "Price unavailable",
                        color    = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Strategy selector ─────────────────────────────
        Text("Strategy", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("hold" to "Buy & Hold", "sell" to "Short Sell")
                .forEach { (value, label) ->
                    val selected = strategy == value
                    Surface(
                        onClick  = { strategy = value },
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (selected) AccentBlue else BgCard,
                        border   = if (!selected) BorderStroke(1.dp, Color(0xFF1A2535)) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            label,
                            color      = if (selected) Color.White else TextSecondary,
                            fontSize   = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                            textAlign  = TextAlign.Center
                        )
                    }
                }
        }

        Spacer(Modifier.height(16.dp))

        // ── Period selector ───────────────────────────────
        if (!useCustomTarget) {
            Text("Investment Period", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))

            val periods = listOf(
                7 to "1W", 14 to "2W", 30 to "1M",
                90 to "3M", 180 to "6M", 365 to "1Y"
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                periods.forEach { (d, label) ->
                    val selected = days == d
                    Surface(
                        onClick  = { days = d },
                        shape    = RoundedCornerShape(8.dp),
                        color    = if (selected) AccentGreen.copy(alpha = 0.15f) else BgCard,
                        border   = BorderStroke(
                            1.dp,
                            if (selected) AccentGreen else Color(0xFF1A2535)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            label,
                            color      = if (selected) AccentGreen else TextSecondary,
                            fontSize   = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            modifier   = Modifier.padding(vertical = 8.dp),
                            textAlign  = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // ── OR divider ────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(color = Color(0xFF1A2535), modifier = Modifier.weight(1f))
            Text("  OR  ", color = TextMuted, fontSize = 12.sp)
            HorizontalDivider(color = Color(0xFF1A2535), modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        // ── My Target toggle ──────────────────────────────
        Surface(
            onClick  = { useCustomTarget = !useCustomTarget },
            shape    = RoundedCornerShape(10.dp),
            color    = if (useCustomTarget) AccentAmber.copy(alpha = 0.15f) else BgCard,
            border   = BorderStroke(
                1.dp,
                if (useCustomTarget) AccentAmber else Color(0xFF1A2535)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (useCustomTarget) "🎯 My Target Mode — ON"
                else "🎯 Set My Own Target Price",
                color      = if (useCustomTarget) AccentAmber else TextSecondary,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                textAlign  = TextAlign.Center
            )
        }

        if (useCustomTarget) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value         = targetPriceText,
                onValueChange = { targetPriceText = it },
                placeholder   = {
                    Text("Enter target price — e.g. 1600", color = TextMuted, fontSize = 13.sp)
                },
                prefix          = {
                    Text("₹", color = AccentAmber, fontWeight = FontWeight.Bold)
                },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AccentAmber,
                    unfocusedBorderColor = Color(0xFF1A2535),
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary,
                    cursorColor          = AccentAmber
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Predict + Buy/Sell buttons ────────────────────
        Button(
            onClick = {
                showDropdown = false
                if (useCustomTarget && targetPriceText.isNotBlank()) {
                    val tp = targetPriceText.toDoubleOrNull()
                    if (tp != null) {
                        viewModel.getTargetPrediction(selectedTicker, tp, strategy)
                    }
                } else {
                    viewModel.getHorizonPrediction(selectedTicker, days, strategy)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (useCustomTarget) AccentAmber else AccentBlue
            )
        ) {
            Text(
                if (useCustomTarget && targetPriceText.isNotBlank())
                    "When will $selectedTicker reach ₹$targetPriceText?"
                else
                    "Predict $selectedTicker for $days days",
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                color      = if (useCustomTarget) Color.Black else Color.White
            )
        }

        // ── BUY / SELL action buttons ─────────────────────
        Spacer(Modifier.height(20.dp))

        // ── Result section ────────────────────────────────
        if (useCustomTarget) {
            when (val pred = targetPrediction) {
                is ApiResult.Loading -> {
                    repeat(3) {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(72.dp).padding(vertical = 4.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = CardDefaults.cardColors(containerColor = BgCard)
                        ) {}
                    }
                }
                is ApiResult.Success -> {
                    TargetResultCard(
                        result     = pred.data,
                        onSetAlert = {
                            android.widget.Toast.makeText(
                                context,
                                "Alert set at ₹${pred.data.notifyAtPrice}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
                is ApiResult.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = AccentRed.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "Error: ${pred.message}",
                            color    = AccentRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = BgCard),
                        border   = BorderStroke(1.dp, Color(0xFF1A2535))
                    ) {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎯", fontSize = 32.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Enter your target price\nthen tap Predict",
                                color     = TextSecondary,
                                fontSize  = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {}
            }
        } else {
            when (val pred = prediction) {
                is ApiResult.Loading -> {
                    repeat(3) {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(72.dp).padding(vertical = 4.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = CardDefaults.cardColors(containerColor = BgCard)
                        ) {}
                    }
                }
                is ApiResult.Success -> {
                    HorizonResultCard(
                        prediction = pred.data,
                        onSetAlert = {
                            AlertScheduler.scheduleAlert(context, pred.data)
                            android.widget.Toast.makeText(
                                context,
                                "Alert set for ${pred.data.notifyDate}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
                is ApiResult.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = AccentRed.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "Error: ${pred.message}",
                            color    = AccentRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = BgCard),
                        border   = BorderStroke(1.dp, Color(0xFF1A2535))
                    ) {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Select stock, strategy\nand period then tap Predict",
                                color     = TextSecondary,
                                fontSize  = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ═══════════════════════════════════════════════════════
// HORIZON RESULT CARD
// ═══════════════════════════════════════════════════════

@Composable
fun HorizonResultCard(
    prediction: HorizonPredictionResponse,
    onSetAlert: () -> Unit
) {
    val signalColor = when (prediction.signal) {
        "BUY"  -> AccentGreen
        "WAIT" -> AccentAmber
        else   -> AccentRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, signalColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(prediction.horizonType, color = TextSecondary, fontSize = 12.sp)
                    Text(prediction.signal, color = signalColor, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text(prediction.ticker, color = TextMuted, fontSize = 12.sp)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = signalColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        "${prediction.confidence}% confidence",
                        color    = signalColor,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HorizonPriceColumn("Current",   "₹${prediction.currentPrice}", null,                         TextPrimary)
                Box(Modifier.width(0.5.dp).height(52.dp).background(Color(0xFF1A2535)))
                HorizonPriceColumn("Target",    "₹${prediction.targetPrice}",  "+${prediction.upsidePct}%",   AccentGreen)
                Box(Modifier.width(0.5.dp).height(52.dp).background(Color(0xFF1A2535)))
                HorizonPriceColumn("Stop Loss", "₹${prediction.stopLoss}",     "-${prediction.downsidePct}%", AccentRed)
            }

            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HorizonDateColumn("Entry",  prediction.entryDate)
                Box(Modifier.width(0.5.dp).height(40.dp).background(Color(0xFF1A2535)))
                HorizonDateColumn("Notify", prediction.notifyDate)
                Box(Modifier.width(0.5.dp).height(40.dp).background(Color(0xFF1A2535)))
                HorizonDateColumn("Target", prediction.targetDate)
            }

            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Risk: ", color = TextSecondary, fontSize = 13.sp)
                val riskColor = when (prediction.riskLevel) {
                    "High"   -> AccentRed
                    "Medium" -> AccentAmber
                    else     -> AccentGreen
                }
                Surface(shape = RoundedCornerShape(6.dp), color = riskColor.copy(alpha = 0.12f)) {
                    Text(prediction.riskLevel, color = riskColor, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            prediction.reasoning.forEach { reason ->
                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                    Text("▸  ", color = signalColor, fontSize = 12.sp)
                    Text(reason, color = TextSecondary, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            Surface(
                shape  = RoundedCornerShape(10.dp),
                color  = AccentBlue.copy(alpha = 0.07f),
                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier        = Modifier.size(36.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Text("🔔", fontSize = 15.sp) }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Notification on ${prediction.notifyDate}", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("80% of your ${prediction.days}-day period", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick  = onSetAlert,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen.copy(alpha = 0.12f)),
                border   = BorderStroke(1.dp, AccentGreen)
            ) {
                Text("🔔  Set Alert for ${prediction.notifyDate}", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Based on technical analysis only. Not financial advice.",
                color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// TARGET RESULT CARD
// ═══════════════════════════════════════════════════════

@Composable
fun TargetResultCard(
    result:     TargetPredictionResponse,
    onSetAlert: () -> Unit
) {
    val probColor = when {
        result.probability >= 65 -> AccentGreen
        result.probability >= 45 -> AccentAmber
        else                     -> AccentRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("🎯 My Target Analysis", color = AccentAmber, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text(result.ticker, color = TextMuted, fontSize = 12.sp)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = probColor.copy(alpha = 0.15f)) {
                    Text("${result.probability}% probability", color = probColor, fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HorizonPriceColumn("Current",   "₹${result.currentPrice}",  null,                          TextPrimary)
                Box(Modifier.width(0.5.dp).height(52.dp).background(Color(0xFF1A2535)))
                HorizonPriceColumn("My Target", "₹${result.targetPrice}",   "+${result.requiredReturn}%",   AccentAmber)
                Box(Modifier.width(0.5.dp).height(52.dp).background(Color(0xFF1A2535)))
                HorizonPriceColumn("Alert at",  "₹${result.notifyAtPrice}", "${result.notifyPct}% of target", AccentBlue)
            }

            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            Text("Expected Timeline", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TimelineColumn("Best Case",  "${result.bestCaseDays}d",  result.bestDate,     AccentGreen)
                TimelineColumn("Expected",   "${result.expectedDays}d",  result.expectedDate, AccentAmber)
                TimelineColumn("Worst Case", "${result.worstCaseDays}d", result.worstDate,    AccentRed)
            }

            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Risk: ", color = TextSecondary, fontSize = 13.sp)
                val riskColor = when (result.riskLevel) {
                    "Very High", "High" -> AccentRed
                    "Medium"            -> AccentAmber
                    else                -> AccentGreen
                }
                Surface(shape = RoundedCornerShape(6.dp), color = riskColor.copy(alpha = 0.12f)) {
                    Text(result.riskLevel, color = riskColor, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            result.reasoning.forEach { reason ->
                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                    Text("▸  ", color = AccentAmber, fontSize = 12.sp)
                    Text(reason, color = TextSecondary, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = Color(0xFF1A2535), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            Surface(
                shape  = RoundedCornerShape(10.dp),
                color  = AccentAmber.copy(alpha = 0.07f),
                border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔔", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Alert when price hits ₹${result.notifyAtPrice}", color = AccentAmber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("${result.notifyPct}% of your target reached", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick  = onSetAlert,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AccentAmber.copy(alpha = 0.12f)),
                border   = BorderStroke(1.dp, AccentAmber)
            ) {
                Text("Set Alert at ₹${result.notifyAtPrice}", color = AccentAmber, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Based on historical price patterns only. Not financial advice.",
                color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═══════════════════════════════════════════════════════

@Composable
fun HorizonPriceColumn(label: String, price: String, sub: String?, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(price, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (sub != null) Text(sub, color = color, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun HorizonDateColumn(label: String, date: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(date, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
fun TimelineColumn(label: String, days: String, date: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        Text(days, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(date, color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}