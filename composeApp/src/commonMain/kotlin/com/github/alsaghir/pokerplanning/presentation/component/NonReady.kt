package com.github.alsaghir.pokerplanning.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pokerplanning.composeapp.generated.resources.Res
import pokerplanning.composeapp.generated.resources.ic_error


@Composable
fun LoadingIndicator() {
    CircularProgressIndicator()
}

@Composable
fun ErrorIcon() {
    Icon(
        painter = painterResource(Res.drawable.ic_error),
        contentDescription = "Something went wrong",
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onPrimary
    )
}