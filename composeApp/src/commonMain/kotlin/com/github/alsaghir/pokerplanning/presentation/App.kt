package com.github.alsaghir.pokerplanning.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import co.touchlab.kermit.Logger
import com.github.alsaghir.pokerplanning.domain.ThemeMode
import com.github.alsaghir.pokerplanning.presentation.model.LocalThemeViewModel
import com.github.alsaghir.pokerplanning.presentation.model.ThemeViewModel
import com.materialkolor.DynamicMaterialTheme
import org.jetbrains.compose.resources.painterResource
import pokerplanning.composeapp.generated.resources.Res
import pokerplanning.composeapp.generated.resources.compose_multiplatform
import pokerplanning.composeapp.generated.resources.ic_casino


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(appContainer: AppContainer = remember { AppContainer(lightColorScheme().primary) }) {

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = windowAdaptiveInfo.windowSizeClass
    val widthDp = windowSizeClass.minWidthDp

    val themeViewModel: ThemeViewModel = viewModel { ThemeViewModel(appContainer.themeRepo) }

    CompositionLocalProvider(
        LocalThemeViewModel provides themeViewModel,
    )
    {

        DisposableEffect(Unit) {
            onDispose { appContainer.close() }
        }

        val theme = themeViewModel.theme.collectAsStateWithLifecycle()

        DynamicMaterialTheme(
            animate = true,
            seedColor = theme.value.toColor(),
            isDark = if (theme.value.mode == ThemeMode.SYSTEM) isSystemInDarkTheme() else theme.value.mode == ThemeMode.DARK,
            style = theme.value.toLibraryPaletteStyle(),

            ) {

            var showContent by remember { mutableStateOf(false) }


            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier
                                    .clickable(onClick = { Logger.e("Not supported") })
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_casino),
                                    contentDescription = "Planning Poker Logo",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                if (widthDp > WIDTH_DP_MEDIUM_LOWER_BOUND) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Planning Poker",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }, actions = {
                            if (widthDp < WIDTH_DP_MEDIUM_LOWER_BOUND) {
                                Text("Action 1")
                            } else {
                                Text("Action 2")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
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