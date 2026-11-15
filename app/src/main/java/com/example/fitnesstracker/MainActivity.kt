package com.example.fitnesstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.ui.LocalAppContainer
import com.example.fitnesstracker.ui.MainViewModel
import com.example.fitnesstracker.ui.navigation.FitTrackerAppRoot
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as FitTrackerApp).container
        setContent {
            val mainViewModel: MainViewModel = viewModel {
                MainViewModel(container.settingsRepository)
            }
            val settings by mainViewModel.settings.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalAppContainer provides container) {
                FitnessTrackerTheme(themeMode = settings.themeMode) {
                    FitTrackerAppRoot()
                }
            }
        }
    }
}
