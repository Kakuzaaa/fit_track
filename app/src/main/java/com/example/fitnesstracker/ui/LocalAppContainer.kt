package com.example.fitnesstracker.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.fitnesstracker.AppContainer

/**
 * CompositionLocal для доступа к DI-контейнеру из composables.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer не предоставлен в Composition")
}
