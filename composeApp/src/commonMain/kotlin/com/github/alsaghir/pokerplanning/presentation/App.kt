package com.github.alsaghir.pokerplanning.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.github.alsaghir.pokerplanning.domain.DataState
import com.github.alsaghir.pokerplanning.domain.ThemeMode
import com.github.alsaghir.pokerplanning.presentation.component.ErrorIcon
import com.github.alsaghir.pokerplanning.presentation.component.LoadingIndicator
import com.github.alsaghir.pokerplanning.presentation.component.TopBar
import com.github.alsaghir.pokerplanning.presentation.model.ThemeUiEvent
import com.github.alsaghir.pokerplanning.presentation.model.ThemeViewModel
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.dynamiccolor.ColorSpec
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.logger.Level
import org.koin.dsl.koinConfiguration
import pokerplanning.composeapp.generated.resources.Res
import pokerplanning.composeapp.generated.resources.compose_multiplatform


val LocalMinWidthDp = compositionLocalOf<Int> {
    error("Not provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {

    KoinApplication(configuration = koinConfiguration {


        modules(appModule)
        printLogger(Level.INFO)


    }) {

        val themeViewModel: ThemeViewModel = koinViewModel<ThemeViewModel>()

        val windowAdaptiveInfo = currentWindowAdaptiveInfo()
        val windowSizeClass = windowAdaptiveInfo.windowSizeClass
        val widthDp = remember(windowSizeClass.minWidthDp) { windowSizeClass.minWidthDp }



        CompositionLocalProvider(
            LocalMinWidthDp provides widthDp
        )
        {

            // better extracted data from view model to be passed down to stateless components
            // but is OK to be collected in other composables as well if needed
            val themeState by themeViewModel.themeState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(themeViewModel) {
                themeViewModel.events.collect { event ->
                    when (event) {
                        is ThemeUiEvent.ShowMessage -> snackbarHostState.showSnackbar(
                            message = event.event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }


            when (val theme = themeState) {
                is DataState.Loading -> {
                    LoadingIndicator()
                }

                is DataState.Error -> {
                    ErrorIcon()
                    Logger.e("Something went wrong")
                }

                is DataState.Success -> {

                    DynamicMaterialTheme(
                        animate = true,
                        seedColor = theme.data.toColor(),
                        isDark = theme.data.mode == ThemeMode.DARK,
                        style = theme.data.toLibraryPaletteStyle(),
                        specVersion = ColorSpec.SpecVersion.SPEC_2025
                    ) {

                        var showContent by remember { mutableStateOf(false) }


                        Scaffold(
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                            topBar = {
                                TopBar(
                                    theme = theme.data,
                                    onSelectThemePalette = { selected ->
                                        themeViewModel.setTheme(theme.data.copy(paletteStyle = selected))
                                    },
                                    onToggleThemeMode = {
                                        themeViewModel.setTheme(
                                            theme.data.copy(
                                                mode = when (theme.data.mode) {
                                                    ThemeMode.DARK -> ThemeMode.SYSTEM
                                                    ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                                    else -> ThemeMode.DARK
                                                }
                                            )
                                        )
                                    })
                            },
                        ) { innerPadding ->
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .background(MaterialTheme.colorScheme.background)
                                    .safeContentPadding()
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Button(onClick = { showContent = !showContent }) {
                                    Text("Click me!")
                                }
                                AnimatedVisibility(showContent) {
                                    val greeting = remember { Greeting().greet() }
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Image(painterResource(Res.drawable.compose_multiplatform), null)
                                        Text("Compose: $greeting")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}