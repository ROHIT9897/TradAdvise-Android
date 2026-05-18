package com.example.stockai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.example.stockai.data.models.HorizonPredictionResponse
import com.example.stockai.data.repository.ApiResult
import com.example.stockai.ui.theme.*
import com.example.stockai.utils.AlertScheduler
import com.example.stockai.viewmodel.StockViewModel

@Composable
fun HorizonScreen(
    ticker:    String,
    viewModel: StockViewModel
) {
    val context        = LocalContext.current
    var days           by remember { mutableStateOf(30) }
    var strategy       by remember { mutableStateOf("hold") }
    val prediction     by viewModel.horizonPrediction.collectAsState()
    var selectedTicker by remember { mutableStateOf(ticker) }
    var searchText     by remember { mutableStateOf("") }
    var showDropdown   by remember { mutableStateOf(false) }

    val popularStocks = listOf(
        "RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK",
        "SBIN", "BHARTIARTL", "WIPRO", "LT", "AXISBANK",
        "KOTAKBANK", "ITC", "SUNPHARMA", "MARUTI", "BAJFINANCE",
        "TITAN", "ADANIENT", "NESTLEIND", "TATAMOTORS", "ZOMATO",
        "HCLTECH", "TECHM", "DRREDDY", "CIPLA", "BAJAJFINSV",
        "TATASTEEL", "JSWSTEEL", "HINDALCO", "ULTRACEMCO", "PNB"
    )

    val filteredStocks = if (searchText.isBlank()) popularStocks
    else popularStocks.filter { it.contains(searchText.uppercase()) }

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
                placeholder   = {
                    Text(
                        "Search — RELIANCE, TCS, INFY...",
                        color    = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon   = {
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
                                horizontal = 8.dp,
                                vertical   = 4.dp
                            )
                        )
                    }
                },
                trailingIcon  = {
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

            // ── Dropdown results ───────────────────────────
            if (showDropdown && filteredStocks.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                        .zIndex(10f),
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BgCard
                    ),
                    border = BorderStroke(1.dp, Color(0xFF1A2535))
                ) {
                    Column {
                        filteredStocks.take(6).forEachIndexed { i, stock ->
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTicker = stock
                                        searchText     = ""
                                        showDropdown   = false
                                    }
                                    .padding(
                                        horizontal = 14.dp,
                                        vertical   = 10.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Transparent circle
                                Box(
                                    modifier        = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            AccentBlue.copy(alpha = 0.12f)
                                        )
                                        .border(
                                            1.dp,
                                            AccentBlue.copy(alpha = 0.25f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        stock.take(2),
                                        color      = AccentBlue,
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.width(10.dp))

                                Text(
                                    stock,
                                    color      = TextPrimary,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (i < filteredStocks.take(6).lastIndex) {
                                HorizontalDivider(
                                    color = Color(0xFF1A2535),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
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
                        color    = if (selected) AccentBlue
                        else BgCard,
                        border   = if (!selected) BorderStroke(
                            1.dp, Color(0xFF1A2535)
                        ) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            label,
                            color      = if (selected) Color.White
                            else TextSecondary,
                            fontSize   = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold
                            else FontWeight.Normal,
                            modifier   = Modifier.padding(
                                horizontal = 8.dp,
                                vertical   = 12.dp
                            ),
                            textAlign  = TextAlign.Center
                        )
                    }
                }
        }

        Spacer(Modifier.height(16.dp))

        // ── Period selector ───────────────────────────────
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
                    color    = if (selected)
                        AccentGreen.copy(alpha = 0.15f)
                    else BgCard,
                    border   = BorderStroke(
                        1.dp,
                        if (selected) AccentGreen
                        else Color(0xFF1A2535)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        label,
                        color      = if (selected) AccentGreen
                        else TextSecondary,
                        fontSize   = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold
                        else FontWeight.Normal,
                        modifier   = Modifier.padding(vertical = 8.dp),
                        textAlign  = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Predict button ────────────────────────────────
        Button(
            onClick  = {
                showDropdown = false
                viewModel.getHorizonPrediction(
                    selectedTicker, days, strategy
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue
            )
        ) {
            Text(
                "Predict $selectedTicker for $days days",
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
                color      = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Result ────────────────────────────────────────
        when (val pred = prediction) {

            is ApiResult.Loading -> {
                repeat(3) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(vertical = 4.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BgCard
                        )
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
                    border   = BorderStroke(
                        1.dp,
                        AccentRed.copy(alpha = 0.3f)
                    )
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
                    colors   = CardDefaults.cardColors(
                        containerColor = BgCard
                    ),
                    border   = BorderStroke(1.dp, Color(0xFF1A2535))
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Select stock, strategy and period\nthen tap Predict",
                            color     = TextSecondary,
                            fontSize  = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {}
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Result Card ───────────────────────────────────────────

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

            // ── Signal + confidence ───────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        prediction.horizonType,
                        color    = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        prediction.signal,
                        color      = signalColor,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        prediction.ticker,
                        color    = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = signalColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        "${prediction.confidence}% confidence",
                        color    = signalColor,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical   = 6.dp
                        )
                    )
                }
            }

            HorizontalDivider(
                color     = Color(0xFF1A2535),
                thickness = 0.5.dp,
                modifier  = Modifier.padding(vertical = 12.dp)
            )

            // ── Price targets ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HorizonPriceColumn(
                    label = "Current",
                    price = "₹${prediction.currentPrice}",
                    sub   = null,
                    color = TextPrimary
                )
                // Divider
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(52.dp)
                        .background(Color(0xFF1A2535))
                )
                HorizonPriceColumn(
                    label = "Target",
                    price = "₹${prediction.targetPrice}",
                    sub   = "+${prediction.upsidePct}%",
                    color = AccentGreen
                )
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(52.dp)
                        .background(Color(0xFF1A2535))
                )
                HorizonPriceColumn(
                    label = "Stop Loss",
                    price = "₹${prediction.stopLoss}",
                    sub   = "-${prediction.downsidePct}%",
                    color = AccentRed
                )
            }

            HorizontalDivider(
                color     = Color(0xFF1A2535),
                thickness = 0.5.dp,
                modifier  = Modifier.padding(vertical = 12.dp)
            )

            // ── Dates ─────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HorizonDateColumn("Entry",  prediction.entryDate)
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(40.dp)
                        .background(Color(0xFF1A2535))
                )
                HorizonDateColumn("Notify", prediction.notifyDate)
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(40.dp)
                        .background(Color(0xFF1A2535))
                )
                HorizonDateColumn("Target", prediction.targetDate)
            }

            HorizontalDivider(
                color     = Color(0xFF1A2535),
                thickness = 0.5.dp,
                modifier  = Modifier.padding(vertical = 12.dp)
            )

            // ── Risk level ────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Risk: ",
                        color    = TextSecondary,
                        fontSize = 13.sp
                    )
                    val riskColor = when (prediction.riskLevel) {
                        "High"   -> AccentRed
                        "Medium" -> AccentAmber
                        else     -> AccentGreen
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = riskColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            prediction.riskLevel,
                            color      = riskColor,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(
                                horizontal = 8.dp,
                                vertical   = 3.dp
                            )
                        )
                    }
                }
                Text(
                    "${prediction.days} day horizon",
                    color    = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Reasoning ─────────────────────────────────
            prediction.reasoning.forEach { reason ->
                Row(
                    modifier          = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "▸  ",
                        color    = signalColor,
                        fontSize = 12.sp
                    )
                    Text(
                        reason,
                        color    = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(
                color     = Color(0xFF1A2535),
                thickness = 0.5.dp,
                modifier  = Modifier.padding(vertical = 12.dp)
            )

            // ── Notification info card ────────────────────
            Surface(
                shape  = RoundedCornerShape(10.dp),
                color  = AccentBlue.copy(alpha = 0.07f),
                border = BorderStroke(
                    1.dp,
                    AccentBlue.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bell icon circle
                    Box(
                        modifier        = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔔", fontSize = 15.sp)
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            "Notification on ${prediction.notifyDate}",
                            color      = AccentBlue,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "80% of your ${prediction.days}-day period — review time",
                            color    = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Set Alert button ──────────────────────────
            Button(
                onClick  = onSetAlert,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, AccentGreen)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🔔", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Set Alert for ${prediction.notifyDate}",
                        color      = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Disclaimer ────────────────────────────────
            Text(
                "Based on technical analysis only. Not financial advice. " +
                        "Always do your own research before investing.",
                color     = TextMuted,
                fontSize  = 10.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Helper composables ────────────────────────────────────

@Composable
fun HorizonPriceColumn(
    label: String,
    price: String,
    sub:   String?,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            label,
            color    = TextSecondary,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            price,
            color      = color,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        if (sub != null) {
            Text(
                sub,
                color    = color,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HorizonDateColumn(label: String, date: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            label,
            color    = TextSecondary,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            date,
            color      = TextPrimary,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center
        )
    }
}