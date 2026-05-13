package com.example.stockai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockai.ui.screens.MainScreen
import com.example.stockai.ui.theme.StockAITheme
import com.example.stockai.viewmodel.StockViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fix edge to edge — content won't go under status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Dark status bar matching app theme
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContent {
            StockAITheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),   // pushes content below status bar
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: StockViewModel = viewModel()
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}