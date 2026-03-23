package com.github.alsaghir.pokerplanning.presentation.model

import androidx.compose.runtime.staticCompositionLocalOf
import com.github.alsaghir.pokerplanning.presentation.AppContainer

/**
 * This is a factory class that provides instances of ViewModels. It takes an AppContainer as a parameter,
 * which is used to access the repositories and other dependencies needed to create the ViewModels.
 * Preventing AppContainer from being directly accessed by the components
 */
class ViewModelFactory(private val appContainer: AppContainer) {
    fun themeViewModel(): ThemeViewModel =
        ThemeViewModel(appContainer.themeRepo)

}

// staticCompositionLocalOf is optimized for values that almost never change
// https://developer.android.com/develop/ui/compose/compositionlocal#creating-apis
val LocalViewModelFactory = staticCompositionLocalOf<ViewModelFactory> {
    error("Not provided")
}