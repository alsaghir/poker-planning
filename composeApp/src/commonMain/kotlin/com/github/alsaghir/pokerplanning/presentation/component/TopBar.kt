package com.github.alsaghir.pokerplanning.presentation.component


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import co.touchlab.kermit.Logger
import com.github.alsaghir.pokerplanning.domain.SerializablePaletteStyle
import com.github.alsaghir.pokerplanning.domain.ThemeDto
import com.github.alsaghir.pokerplanning.domain.ThemeMode
import com.github.alsaghir.pokerplanning.presentation.LocalMinWidthDp
import org.jetbrains.compose.resources.painterResource
import pokerplanning.composeapp.generated.resources.Res
import pokerplanning.composeapp.generated.resources.ic_casino
import pokerplanning.composeapp.generated.resources.ic_colorize
import pokerplanning.composeapp.generated.resources.ic_computer
import pokerplanning.composeapp.generated.resources.ic_dark_mode
import pokerplanning.composeapp.generated.resources.ic_light_mode
import pokerplanning.composeapp.generated.resources.ic_palette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    theme: ThemeDto,
    onSelectThemePalette: (SerializablePaletteStyle) -> Unit,
    onToggleThemeMode: () -> Unit,
) {
    val minWidthDp = LocalMinWidthDp.current

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
                if (minWidthDp > WIDTH_DP_MEDIUM_LOWER_BOUND) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Planning Poker",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            if (minWidthDp < WIDTH_DP_MEDIUM_LOWER_BOUND) {
                Text("Action 1")
            } else {
                Text("Action 2")
            }

            ThemePaletteMenu(
                selectedPalette = theme.paletteStyle,
                onSelectPalette = onSelectThemePalette
            )

            IconMenuItem(
                painter = painterResource(
                    when (theme.mode) {
                        ThemeMode.DARK -> {
                            Res.drawable.ic_light_mode
                        }

                        ThemeMode.SYSTEM -> {
                            Res.drawable.ic_computer
                        }

                        else -> {
                            Res.drawable.ic_dark_mode
                        }
                    }
                ),
                contentDescription = when (theme.mode) {
                    ThemeMode.DARK -> {
                        "Switch to System Mode"
                    }

                    ThemeMode.SYSTEM -> {
                        "Switch to Light Mode"
                    }

                    else -> {
                        "Switch to Dark Mode"
                    }
                },
                tooltipText = when (theme.mode) {
                    ThemeMode.DARK -> {
                        "Switch to System Mode"
                    }

                    ThemeMode.SYSTEM -> {
                        "Switch to Light Mode"
                    }

                    else -> {
                        "Switch to Dark Mode"
                    }
                },
                onClick = onToggleThemeMode
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun ThemePaletteMenu(
    selectedPalette: SerializablePaletteStyle,
    onSelectPalette: (SerializablePaletteStyle) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // Theme selector button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { expanded = true }
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_palette),
                contentDescription = "Theme",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = selectedPalette.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SerializablePaletteStyle.entries.forEach { themeType ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = themeType.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onSelectPalette(themeType)
                        expanded = false
                    },
                    leadingIcon = {
                        if (themeType == selectedPalette) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_palette),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_colorize),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}