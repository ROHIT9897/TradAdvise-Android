// ui/screens/MainScreen.kt
package com.example.stockai.ui.screens

import androidx.compose.runtime.MutableState
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.stockai.data.models.*
import com.example.stockai.data.repository.ApiResult
import com.example.stockai.viewmodel.StockViewModel
import kotlin.math.abs

// ── Design tokens ────────────────────────────────────────
val BgDeep    = Color(0xFF070B14)
val BgCard    = Color(0xFF0F1923)
val BgCard2   = Color(0xFF152030)
val AccentGreen  = Color(0xFF00E5A0)
val AccentRed    = Color(0xFFFF4560)
val AccentBlue   = Color(0xFF2979FF)
val AccentAmber  = Color(0xFFFFB300)
val TextPrimary  = Color(0xFFF0F4FF)
val TextSecondary = Color(0xFF6B7FA3)
val TextMuted    = Color(0xFF3A4A6B)

fun signalColor(signal: String) = when (signal) {
    "BUY"  -> AccentGreen
    "SELL" -> AccentRed
    else   -> AccentBlue
}

fun signalBg(signal: String) = when (signal) {
    "BUY"  -> Color(0xFF00E5A0).copy(alpha = 0.08f)
    "SELL" -> Color(0xFFFF4560).copy(alpha = 0.08f)
    else   -> Color(0xFF2979FF).copy(alpha = 0.08f)
}

// ── Main screen ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun MainScreen(viewModel: StockViewModel, selectedTab: MutableState<Int>) {
    val prediction     by viewModel.prediction.collectAsState()
    val livePrice      by viewModel.livePrice.collectAsState()
    val movers         by viewModel.movers.collectAsState()
    val chartData      by viewModel.chartData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val currentTicker  by viewModel.currentTicker.collectAsState()
    val isRefreshing   by viewModel.isRefreshing.collectAsState()
    val suggestions    by viewModel.suggestions.collectAsState()
    val watchlist      by viewModel.watchlist.collectAsState()

    var searchText  by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDeep)
        ) {
            TopBar(
                ticker = currentTicker,
                onRefresh = { viewModel.refresh() },
                isRefreshing = isRefreshing
            )

            SearchBarRow(
                value = searchText,
                onChange = {
                    searchText = it.uppercase()
                    viewModel.updateSuggestions(it)
                },
                onSearch = {
                    if (searchText.isNotBlank()) {
                        viewModel.analyzeStock(searchText)
                        searchText = ""
                        selectedTab.value = 0        // ← switch to Overview on search
                    }
                },
                suggestions = suggestions,
                onSuggestionClick = {
                    viewModel.analyzeStock(it)
                    searchText = ""
                    selectedTab.value = 0            // ← switch to Overview on suggestion tap
                }
            )

            StockTabBar(
                selected = selectedTab.value,
                onSelect = { selectedTab.value = it }
            )

            when (selectedTab.value) {
                0 -> OverviewTab(
                    prediction = prediction,
                    livePrice = livePrice,
                    chartData = chartData,
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { period ->
                        viewModel.loadChartData(currentTicker, period)
                    }
                )

                1 -> IndicatorsTab(prediction)
                2 -> NewsTab(prediction)
                3 -> MoversTab(
                    movers = movers,
                    watchlist = watchlist,
                    viewModel = viewModel,
                    onAnalyze = { selectedTab.value = 0 }
                )

                4 -> HorizonScreen(
                    ticker = currentTicker,
                    viewModel = viewModel
                )
            }
        }
        if (selectedTab.value != 3) {
            WatchlistFab(
                watchlistCount = watchlist.size,
                currentTicker  = currentTicker,
                isInWatchlist  = watchlist.contains(currentTicker),
                onAddRemove    = {
                    if (watchlist.contains(currentTicker)) {
                        viewModel.removeFromWatchlist(currentTicker)
                    } else {
                        viewModel.addToWatchlist(currentTicker)
                    }
                },
                onOpenWatchlist = {
                    selectedTab.value = 3  // Go to Market tab
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 20.dp)
            )
        }
    }
}
// ── Top bar ──────────────────────────────────────────────

@Composable
fun WatchlistFab(
    watchlistCount:  Int,
    currentTicker:   String,
    isInWatchlist:   Boolean,
    onAddRemove:     () -> Unit,
    onOpenWatchlist: () -> Unit,
    modifier:        Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ── Mini action buttons (appear when expanded) ─────
        if (expanded) {

            // Add/Remove current stock
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Label
                Surface(
                    shape  = RoundedCornerShape(8.dp),
                    color  = BgCard,
                    border = BorderStroke(1.dp, Color(0xFF1A2535))
                ) {
                    Text(
                        if (isInWatchlist) "Remove $currentTicker"
                        else "Add $currentTicker",
                        color    = if (isInWatchlist) AccentRed else AccentGreen,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical   = 6.dp
                        )
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Mini circle button
                Box(
                    modifier        = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isInWatchlist)
                                AccentRed.copy(alpha = 0.15f)
                            else
                                AccentGreen.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (isInWatchlist) AccentRed.copy(alpha = 0.5f)
                            else AccentGreen.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable {
                            onAddRemove()
                            expanded = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isInWatchlist) "✕" else "+",
                        color      = if (isInWatchlist) AccentRed else AccentGreen,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Open watchlist
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape  = RoundedCornerShape(8.dp),
                    color  = BgCard,
                    border = BorderStroke(1.dp, Color(0xFF1A2535))
                ) {
                    Text(
                        "View Watchlist ($watchlistCount)",
                        color    = AccentBlue,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical   = 6.dp
                        )
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier        = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            AccentBlue.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable {
                            onOpenWatchlist()
                            expanded = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("★", color = AccentBlue, fontSize = 16.sp)
                }
            }
        }

        // ── Main transparent FAB ───────────────────────────
        Box(
            modifier        = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(BgCard.copy(alpha = 0.85f))
                .border(
                    1.5.dp,
                    if (isInWatchlist)
                        AccentGreen.copy(alpha = 0.6f)
                    else
                        AccentBlue.copy(alpha = 0.4f),
                    CircleShape
                )
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (isInWatchlist) "★" else "☆",
                    color    = if (isInWatchlist) AccentGreen else AccentBlue,
                    fontSize = 20.sp
                )
                if (watchlistCount > 0) {
                    Text(
                        watchlistCount.toString(),
                        color    = TextMuted,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(ticker: String, onRefresh: () -> Unit, isRefreshing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "TradAdvise",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Analyzing $ticker",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        val rotation by animateFloatAsState(
            targetValue = if (isRefreshing) 360f else 0f,
            animationSpec = if (isRefreshing)
                infiniteRepeatable(tween(800), RepeatMode.Restart)
            else tween(0),
            label = "refresh_rotation"
        )

        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(40.dp)
                .background(BgCard2, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = AccentBlue,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

// ── Search bar ───────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarRow(
    value: String,
    onChange: (String) -> Unit,
    onSearch: () -> Unit,
    suggestions: List<Pair<String, String>>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = value,
                onValueChange = onChange,
                placeholder   = {
                    Text(
                        "Search — INFY, TCS, Reliance...",
                        color    = TextMuted,
                        fontSize = 13.sp
                    )
                },
                singleLine  = true,
                modifier    = Modifier.weight(1f).height(52.dp),
                colors      = OutlinedTextFieldDefaults.colors(
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    focusedBorderColor      = AccentBlue,
                    unfocusedBorderColor    = TextMuted,
                    focusedContainerColor   = BgCard,
                    unfocusedContainerColor = BgCard,
                    cursorColor             = AccentBlue
                ),
                shape     = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Button(
                onClick  = onSearch,
                modifier = Modifier.height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            }
        }

        // Suggestions dropdown
        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard2),
                border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
            ) {
                Column {
                    suggestions.forEachIndexed { index, (ticker, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClick(ticker) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    ticker,
                                    color      = TextPrimary,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    name,
                                    color    = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint     = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (index < suggestions.size - 1) {
                            HorizontalDivider(
                                color     = TextMuted.copy(alpha = 0.15f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Tab bar ──────────────────────────────────────────────

@Composable
fun StockTabBar(selected: Int, onSelect: (Int) -> Unit) {

    val tabs = listOf(
        "Overview", "Indicators", "News", "Market", "Horizon"
    )

    ScrollableTabRow(
        selectedTabIndex = selected,           // ← selectedTab.value nahi
        containerColor   = BgCard,
        contentColor     = AccentBlue,
        edgePadding      = 0.dp
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selected == index,          // ← selected
                onClick  = { onSelect(index) },        // ← onSelect
                text     = {
                    Text(
                        title,
                        fontSize   = 11.sp,
                        fontWeight = if (selected == index)
                            FontWeight.Bold
                        else FontWeight.Normal,
                        maxLines   = 1
                    )
                }
            )
        }
    }
}

// ── OVERVIEW TAB ─────────────────────────────────────────

@Composable
fun OverviewTab(
    prediction:     ApiResult<PredictionResponse>?,
    livePrice:      ApiResult<LivePriceResponse>?,
    chartData:      ApiResult<ChartResponse>?,       // ADD
    selectedPeriod: String,                           // ADD
    onPeriodChange: (String) -> Unit                  // ADD
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            when (livePrice) {
                is ApiResult.Loading -> ShimmerCard(height = 100)
                is ApiResult.Success -> LivePriceCard(livePrice.data)
                is ApiResult.Error   -> ErrorCard(livePrice.message)
                null -> {}
            }
        }

        item {
            StockChartCard(
                chartData      = chartData,
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange
            )
        }

        item {
            when (prediction) {
                is ApiResult.Loading -> ShimmerCard(height = 200)
                is ApiResult.Success -> SignalCard(prediction.data)
                is ApiResult.Error   -> ErrorCard(prediction.message)
                null -> {}
            }
        }

        item {
            when (prediction) {
                is ApiResult.Success -> AgreementCard(prediction.data)
                else -> {}
            }
        }

        item {
            when (prediction) {
                is ApiResult.Success -> ExplanationCard(prediction.data.explanation)
                else -> {}
            }
        }

        item {
            when (prediction) {
                is ApiResult.Success -> ValidationCard(prediction.data.validation)
                else -> {}
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
// ── Live price card ──────────────────────────────────────

@Composable
fun LivePriceCard(price: LivePriceResponse) {
    val isPositive = price.changePct >= 0
    val changeColor = if (isPositive) AccentGreen else AccentRed
    val arrow = if (isPositive) "▲" else "▼"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(price.ticker, color = TextSecondary, fontSize = 12.sp,
                    fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹${String.format("%,.2f", price.price)}",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("H: ₹${price.dayHigh}", color = AccentGreen, fontSize = 11.sp)
                    Text("L: ₹${price.dayLow}",  color = AccentRed,   fontSize = 11.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = changeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$arrow ${String.format("%.2f", price.changePct)}%",
                        color = changeColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$arrow ₹${String.format("%.2f", abs(price.change))}",
                    color = changeColor.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ── Signal card ──────────────────────────────────────────
@Composable
fun StockChartCard(
    chartData:      ApiResult<ChartResponse>?,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit
) {
    val periods = listOf("1W", "1M", "3M", "6M", "1Y")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Header ────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Price Chart",
                    color      = TextPrimary,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Period selector tabs
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    periods.forEach { period ->
                        val isSelected = period == selectedPeriod
                        Surface(
                            shape    = RoundedCornerShape(6.dp),
                            color    = if (isSelected) AccentBlue
                            else TextMuted.copy(alpha = 0.1f),
                            modifier = Modifier.clickable {
                                onPeriodChange(period)
                            }
                        ) {
                            Text(
                                text       = period,
                                color      = if (isSelected) Color.White else TextSecondary,
                                fontSize   = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold
                                else FontWeight.Normal,
                                modifier   = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical   = 4.dp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Chart content ─────────────────────────
            when (chartData) {
                is ApiResult.Loading -> {
                    Box(
                        modifier          = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment  = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(28.dp),
                                color       = AccentBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Loading chart...",
                                color    = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                is ApiResult.Success -> {
                    val prices = chartData.data.prices
                    if (prices.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No chart data", color = TextSecondary)
                        }
                    } else {
                        RealLineChart(prices = prices)

                        Spacer(Modifier.height(12.dp))

                        // Stats row
                        ChartStatsRow(prices = prices)
                    }
                }

                is ApiResult.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Chart unavailable: ${chartData.message}",
                            color    = AccentRed,
                            fontSize = 12.sp
                        )
                    }
                }

                null -> {}
            }
        }
    }
}


@Composable
fun RealLineChart(prices: List<PricePoint>) {
    val closePrices = prices.map { it.close.toFloat() }
    val maxPrice    = closePrices.max()
    val minPrice    = closePrices.min()
    val range       = (maxPrice - minPrice).takeIf { it > 0 } ?: 1f

    // Determine if overall trend is up or down
    val isUptrend   = closePrices.last() >= closePrices.first()
    val lineColor   = if (isUptrend) AccentGreen else AccentRed

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width  = size.width
        val height = size.height
        val count  = closePrices.size
        val stepX  = width / (count - 1).coerceAtLeast(1)

        // ── Draw horizontal grid lines ─────────────
        val gridCount = 4
        repeat(gridCount) { i ->
            val y = height * i / (gridCount - 1)
            drawLine(
                color       = Color(0xFF1A2535),
                start       = Offset(0f, y),
                end         = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // ── Build line path ────────────────────────
        val linePath = androidx.compose.ui.graphics.Path()
        closePrices.forEachIndexed { i, price ->
            val x = i * stepX
            val y = height - ((price - minPrice) / range * height * 0.9f) - height * 0.05f
            if (i == 0) linePath.moveTo(x, y)
            else        linePath.lineTo(x, y)
        }

        // ── Fill gradient under line ───────────────
        val fillPath = androidx.compose.ui.graphics.Path().apply {
            addPath(linePath)
            lineTo((count - 1) * stepX, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.25f),
                    lineColor.copy(alpha = 0.0f)
                )
            )
        )

        // ── Draw price line ────────────────────────
        drawPath(
            path  = linePath,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.5f,
                cap   = androidx.compose.ui.graphics.StrokeCap.Round,
                join  = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )

        // ── Draw current price dot ─────────────────
        val lastX = (count - 1) * stepX
        val lastY = height - (
                (closePrices.last() - minPrice) / range * height * 0.9f
                ) - height * 0.05f

        drawCircle(
            color  = lineColor,
            radius = 8f,
            center = Offset(lastX, lastY),
            alpha  = 0.3f
        )
        drawCircle(
            color  = lineColor,
            radius = 5f,
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color  = Color.White,
            radius = 2.5f,
            center = Offset(lastX, lastY)
        )
    }
}


@Composable
fun ChartStatsRow(prices: List<PricePoint>) {
    val closes    = prices.map { it.close }
    val firstPrice = closes.first()
    val lastPrice  = closes.last()
    val change     = lastPrice - firstPrice
    val changePct  = (change / firstPrice) * 100
    val high       = prices.maxOf { it.high }
    val low        = prices.minOf { it.low }

    val isPositive = change >= 0
    val changeColor = if (isPositive) AccentGreen else AccentRed

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChartStat(
            label = "Change",
            value = "${if (isPositive) "+" else ""}${
                String.format(java.util.Locale.getDefault(), "%.2f", changePct)
            }%",
            color = changeColor
        )
        ChartStat(
            label = "High",
            value = "₹${String.format(java.util.Locale.getDefault(), "%.0f", high)}",
            color = AccentGreen
        )
        ChartStat(
            label = "Low",
            value = "₹${String.format(java.util.Locale.getDefault(), "%.0f", low)}",
            color = AccentRed
        )
        ChartStat(
            label = "Points",
            value = "${prices.size}",
            color = TextSecondary
        )
    }
}


@Composable
fun ChartStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted,  fontSize = 10.sp)
        Text(value, color = color,      fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun SignalCard(pred: PredictionResponse) {
    val signal   = pred.validation.finalSignal
    val color    = signalColor(signal)
    val bgColor  = signalBg(signal)

    val animatedScore by animateFloatAsState(
        targetValue = (pred.score / 100).toFloat(),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "score_anim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Signal badge
                Column {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = bgColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val emoji = when (signal) {
                                "BUY"  -> "📈"
                                "SELL" -> "📉"
                                else   -> "⏸"
                            }
                            Text(emoji, fontSize = 20.sp)
                            Text(
                                text = signal,
                                color = color,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (pred.validation.isValidated)
                            "✓ Signal validated" else "○ Awaiting confirmation",
                        color = if (pred.validation.isValidated) color else TextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Score gauge
                ScoreGauge(score = animatedScore, color = color, label = "${pred.score.toInt()}")
            }

            Spacer(Modifier.height(16.dp))

            // Score bar
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Signal Score", color = TextSecondary, fontSize = 12.sp)
                    Text("${pred.score.toInt()}/100", color = color, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = animatedScore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color      = color,
                    trackColor = TextMuted.copy(alpha = 0.3f)
                )
            }

            // Sentiment row
            if (pred.sentimentScore != 0.0 || pred.signalNote.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))
                Spacer(Modifier.height(12.dp))

                val sentColor = when (pred.sentiment) {
                    "BULLISH" -> AccentGreen
                    "BEARISH" -> AccentRed
                    else      -> TextSecondary
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sentEmoji = when (pred.sentiment) {
                        "BULLISH" -> "📰🟢"
                        "BEARISH" -> "📰🔴"
                        else      -> "📰⚪"
                    }
                    Text(sentEmoji, fontSize = 14.sp)
                    Text("News: ", color = TextSecondary, fontSize = 13.sp)
                    Text(pred.sentiment, color = sentColor, fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold)
                    if (pred.signalNote.isNotBlank()) {
                        Text("· ${pred.signalNote}", color = TextSecondary,
                            fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f))
                    }
                }
            }

            // Blocked reasons
            if (pred.validation.blockedReasons.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                pred.validation.blockedReasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            Modifier.size(6.dp)
                                .background(AccentAmber, CircleShape)
                        )
                        Text(reason, color = AccentAmber, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreGauge(score: Float, color: Color, label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(72.dp)
    ) {
        CircularProgressIndicator(
            progress = {score},
            modifier = Modifier.fillMaxSize(),
            color      = color,
            strokeWidth = 6.dp,
            trackColor = TextMuted.copy(alpha = 0.2f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("score", color = TextSecondary, fontSize = 9.sp)
        }
    }
}

// ── Agreement card ───────────────────────────────────────

@Composable
fun AgreementCard(pred: PredictionResponse) {
    val total = pred.bullIndicators + pred.bearIndicators
    val bullFraction = if (total > 0) pred.bullIndicators.toFloat() / total else 0.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Indicator Agreement", color = TextPrimary, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold)
                Text(pred.agreement, color = AccentBlue, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            // Bull vs Bear bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(AccentRed.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(bullFraction)
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentGreen.copy(alpha = 0.8f), AccentGreen)
                            )
                        )
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🟢 ${pred.bullIndicators} Bullish", color = AccentGreen, fontSize = 12.sp)
                Text("🔴 ${pred.bearIndicators} Bearish", color = AccentRed,   fontSize = 12.sp)
            }
        }
    }
}

// ── Explanation card ─────────────────────────────────────

@Composable
fun ExplanationCard(reasons: List<String>) {
    if (reasons.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Why this signal?", color = TextPrimary, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            reasons.forEach { reason ->
                Row(
                    modifier = Modifier.padding(bottom = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .padding(top = 6.dp)
                            .size(5.dp)
                            .background(AccentBlue, CircleShape)
                    )
                    Text(reason, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

// ── Validation card ──────────────────────────────────────

@Composable
fun ValidationCard(validation: Validation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Validation Checks", color = TextPrimary, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            validation.passedChecks.forEach { check ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text("✓", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(check, color = TextSecondary, fontSize = 12.sp)
                }
            }

            validation.blockedReasons.forEach { reason ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text("✗", color = AccentRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(reason, color = AccentAmber, fontSize = 12.sp)
                }
            }
        }
    }
}

// ── INDICATORS TAB ───────────────────────────────────────

@Composable
fun IndicatorsTab(prediction: ApiResult<PredictionResponse>?) {
    when (prediction) {
        is ApiResult.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        is ApiResult.Success -> {
            val pred = prediction.data
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { IndividualSignalsCard(pred.individualSignals) }
                item { RsiCard(pred.indicators.rsi) }
                item { BollingerCard(pred.indicators.bbPosition) }
                item { VolumeCard(pred.indicators.volRatio) }
                item { MaTrendCard(pred.indicators.ma20Slope) }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
        is ApiResult.Error -> ErrorCard(prediction.message)
        null -> {}
    }
}

@Composable
fun IndividualSignalsCard(signals: IndividualSignals) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Signal Breakdown", color = TextPrimary, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            val items = listOf(
                "Trend"    to signals.trend,
                "Momentum" to signals.momentum,
                "MACD"     to signals.macd,
                "Bands"    to signals.bands,
                "Volume"   to signals.volume,
            )

            items.forEach { (label, value) ->
                SignalRow(label = label, value = value)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun SignalRow(label: String, value: Double) {
    val color = when {
        value > 0  -> AccentGreen
        value < 0  -> AccentRed
        else       -> TextSecondary
    }
    val indicator = when {
        value > 0  -> "▲ Bullish"
        value < 0  -> "▼ Bearish"
        else       -> "● Neutral"
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.12f)) {
            Text(
                indicator, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun RsiCard(rsi: Double) {
    val color = when {
        rsi > 70 -> AccentRed
        rsi < 30 -> AccentGreen
        else     -> AccentBlue
    }
    val label = when {
        rsi > 70 -> "Overbought"
        rsi < 30 -> "Oversold"
        else     -> "Neutral"
    }

    GaugeCard(
        title    = "RSI (14)",
        value    = rsi,
        progress = (rsi / 100).toFloat(),
        label    = label,
        color    = color,
        min      = "0",
        max      = "100"
    )
}

@Composable
fun BollingerCard(bbPos: Double) {
    val color = when {
        bbPos > 0.8 -> AccentRed
        bbPos < 0.2 -> AccentGreen
        else        -> AccentBlue
    }
    val label = when {
        bbPos > 0.8 -> "Near Upper Band"
        bbPos < 0.2 -> "Near Lower Band"
        else        -> "Middle Zone"
    }

    GaugeCard(
        title    = "Bollinger Band Position",
        value    = bbPos * 100,
        progress = bbPos.toFloat().coerceIn(0f, 1f),
        label    = label,
        color    = color,
        min      = "Lower",
        max      = "Upper"
    )
}

@Composable
fun VolumeCard(volRatio: Double) {
    val color = if (volRatio > 1.2) AccentGreen else TextSecondary
    val label = when {
        volRatio > 2.0 -> "Very High Volume"
        volRatio > 1.2 -> "Above Average"
        volRatio < 0.8 -> "Below Average"
        else           -> "Normal Volume"
    }

    GaugeCard(
        title    = "Volume Ratio",
        value    = volRatio,
        progress = (volRatio / 3f).toFloat().coerceIn(0f, 1f),
        label    = label,
        color    = color,
        min      = "0x",
        max      = "3x+"
    )
}

@Composable
fun MaTrendCard(ma20Slope: Double) {
    val color = if (ma20Slope > 0) AccentGreen else AccentRed
    val label = if (ma20Slope > 0) "Upward Trend" else "Downward Trend"
    val progress = ((ma20Slope + 5) / 10f).toFloat().coerceIn(0f, 1f)

    GaugeCard(
        title    = "MA20 Trend Slope",
        value    = ma20Slope,
        progress = progress,
        label    = label,
        color    = color,
        min      = "Bearish",
        max      = "Bullish"
    )
}

@Composable
fun GaugeCard(
    title: String, value: Double, progress: Float,
    label: String, color: Color, min: String, max: String
) {
    val animProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(800, easing = EaseOutCubic),
        label         = "gauge_$title"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.12f)) {
                    Text(
                        text = label, color = color, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = String.format(java.util.Locale.getDefault(), "%.2f", value),
                color = color, fontSize = 24.sp, fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress      = {animProgress},
                modifier      = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color         = color,
                trackColor    = TextMuted.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(4.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(min, color = TextMuted, fontSize = 10.sp)
                Text(max, color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

// ── NEWS TAB ─────────────────────────────────────────────

@Composable
fun NewsTab(prediction: ApiResult<PredictionResponse>?) {
    when (prediction) {
        is ApiResult.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        is ApiResult.Success -> {
            val news = prediction.data.news
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { NewsSummaryCard(news) }
                items(news.articles) { article ->
                    NewsArticleCard(article)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
        is ApiResult.Error -> ErrorCard(prediction.message)
        null -> {}
    }
}

@Composable
fun NewsSummaryCard(news: NewsResponse) {
    val sentColor = when (news.overallSentiment) {
        "BULLISH" -> AccentGreen
        "BEARISH" -> AccentRed
        else      -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, sentColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Market Sentiment", color = TextPrimary, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold)
                Surface(shape = RoundedCornerShape(8.dp), color = sentColor.copy(alpha = 0.12f)) {
                    Text(
                        news.overallSentiment, color = sentColor,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SentimentPill("🟢 ${news.breakdown.positive}", AccentGreen)
                SentimentPill("🔴 ${news.breakdown.negative}", AccentRed)
                SentimentPill("⚪ ${news.breakdown.neutral}",  TextSecondary)
            }
        }
    }
}

@Composable
fun SentimentPill(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.1f)) {
        Text(
            text = text, color = color, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun NewsArticleCard(article: NewsArticle) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val sentColor = when (article.sentiment) {
        "POSITIVE" -> AccentGreen
        "NEGATIVE" -> AccentRed
        else       -> TextSecondary
    }
    val sentEmoji = when (article.sentiment) {
        "POSITIVE" -> "🟢"
        "NEGATIVE" -> "🔴"
        else       -> "⚪"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // ADD THIS — makes card tappable
            .clickable {
                if (article.url.isNotBlank()) {
                    try {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(article.url)
                        )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // URL could not be opened
                    }
                }
            },
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard2),
        border   = BorderStroke(1.dp, TextMuted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text     = article.title,
                color    = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(article.source, color = TextSecondary, fontSize = 11.sp)
                    Text("·", color = TextMuted, fontSize = 11.sp)
                    // Tap to read label
                    Text(
                        "Tap to read",
                        color    = AccentBlue,
                        fontSize = 10.sp
                    )
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(sentEmoji, fontSize = 12.sp)
                    Text(
                        article.sentiment,
                        color      = sentColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── MARKET MOVERS TAB ────────────────────────────────────

@Composable
fun MoversTab(
    movers:    ApiResult<MoversResponse>?,
    watchlist: Set<String>,
    viewModel: StockViewModel,
    onAnalyze: () -> Unit              // ADD THIS
) {
    when (movers) {
        is ApiResult.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }

        is ApiResult.Success -> {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("⭐ My Watchlist", color = TextPrimary,
                            fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${watchlist.size} stocks",
                            color = TextSecondary, fontSize = 12.sp)
                    }
                }

                items(watchlist.toList()) { ticker ->
                    WatchlistCard(
                        ticker    = ticker,
                        onAnalyze = {
                            viewModel.analyzeStock(ticker)
                            onAnalyze()            // ← switches tab to Overview
                        },
                        onRemove  = { viewModel.removeFromWatchlist(ticker) }
                    )
                }

                item {
                    AddToWatchlistRow { ticker ->
                        viewModel.addToWatchlist(ticker)
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
                    Text("📈 Top Gainers", color = TextPrimary,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                items(movers.data.topGainers) { stock ->
                    MoverCard(
                        stock     = stock,
                        watchlist = watchlist,
                        viewModel = viewModel,
                        onAnalyze = onAnalyze      // ← pass down
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }

                item {
                    Text("📉 Top Losers", color = TextPrimary,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                items(movers.data.topLosers) { stock ->
                    MoverCard(
                        stock     = stock,
                        watchlist = watchlist,
                        viewModel = viewModel,
                        onAnalyze = onAnalyze      // ← pass down
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        is ApiResult.Error -> ErrorCard(movers.message)
        null -> {}
    }
}

@Composable
fun WatchlistCard(
    ticker:    String,
    onAnalyze: () -> Unit,
    onRemove:  () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Left — tap to analyze
            Row(
                modifier  = Modifier
                    .weight(1f)
                    .clickable { onAnalyze() },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ticker circle
                Box(
                    modifier         = Modifier
                        .size(42.dp)
                        .background(AccentBlue.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = ticker.take(2),
                        color      = AccentBlue,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Text(
                        ticker,
                        color      = TextPrimary,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap to analyze →",
                        color    = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Remove button — matches theme
            IconButton(
                onClick  = onRemove,
                modifier = Modifier
                    .size(32.dp)
                    .background(AccentRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint     = AccentRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddToWatchlistRow(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value         = text,
            onValueChange = { text = it.uppercase() },
            placeholder   = {
                Text("Add ticker to watchlist...", color = TextMuted, fontSize = 13.sp)
            },
            singleLine = true,
            modifier   = Modifier.weight(1f).height(50.dp),
            colors     = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                focusedBorderColor      = AccentGreen,
                unfocusedBorderColor    = TextMuted,
                focusedContainerColor   = BgCard,
                unfocusedContainerColor = BgCard,
                cursorColor             = AccentGreen
            ),
            shape     = RoundedCornerShape(10.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        // Green + button — matches theme
        Button(
            onClick  = {
                if (text.isNotBlank()) {
                    onAdd(text)
                    text = ""
                }
            },
            modifier = Modifier
                .height(50.dp)
                .width(50.dp),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint     = Color(0xFF070B14),   // dark icon on green
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun WatchlistItem(
    ticker:    String,
    isPremium: Boolean,
    onClick:   () -> Unit,
    onRemove:  () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, Color(0xFF1A2535))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Transparent circle with initials ──────────
            Box(
                modifier        = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        AccentBlue.copy(alpha = 0.15f)  // transparent
                    )
                    .border(
                        1.dp,
                        AccentBlue.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = ticker.take(2),
                    color      = AccentBlue,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            // ── Ticker name ───────────────────────────────
            Text(
                ticker,
                color      = TextPrimary,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.weight(1f)
            )

            // ── Remove button ─────────────────────────────
            IconButton(onClick = onRemove) {
                Text("✕", color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}
// Update MoverCard to show watchlist add/remove button
@Composable
fun MoverCard(
    stock:     LivePriceResponse,
    watchlist: Set<String>,
    viewModel: StockViewModel,
    onAnalyze: () -> Unit              // ADD THIS
) {
    val isWatchlisted = watchlist.contains(stock.ticker)
    val isPositive    = stock.changePct >= 0
    val color         = if (isPositive) AccentGreen else AccentRed
    val arrow         = if (isPositive) "▲" else "▼"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Stock info — tap to analyze AND switch tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        viewModel.analyzeStock(stock.ticker)
                        onAnalyze()        // ← switches to Overview tab
                    }
            ) {
                Text(
                    stock.ticker,
                    color      = TextPrimary,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "₹${stock.price}",
                    color    = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Change badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Text(
                    "$arrow ${String.format(java.util.Locale.getDefault(), "%.2f", stock.changePct)}%",
                    color      = color,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Watchlist star button
            IconButton(
                onClick  = {
                    if (isWatchlisted) viewModel.removeFromWatchlist(stock.ticker)
                    else viewModel.addToWatchlist(stock.ticker)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector        = if (isWatchlisted) Icons.Default.Star
                    else Icons.Default.StarBorder,
                    contentDescription = if (isWatchlisted) "Remove" else "Add",
                    tint               = if (isWatchlisted) AccentGreen else TextMuted,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}
// ── Shimmer loading ──────────────────────────────────────

@Composable
fun ShimmerCard(height: Int) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -400f, targetValue = 400f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "shimmer_x"
    )

    val brush = Brush.linearGradient(
        colors = listOf(BgCard, BgCard2, BgCard),
        start  = Offset(translateX, 0f),
        end    = Offset(translateX + 400f, 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
    )
}

// ── Error card ───────────────────────────────────────────

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.1f)),
        border   = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("⚠", fontSize = 18.sp)
            Text(message, color = AccentRed, fontSize = 13.sp)
        }
    }
}