package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.drew654.mocklocations.presentation.NoRippleInteractionSource

@Composable
fun DisableableSmallFloatingActionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    SmallFloatingActionButton(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        containerColor = if (enabled)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        interactionSource = remember(enabled) { if (enabled) MutableInteractionSource() else NoRippleInteractionSource() },
        modifier = modifier
    ) {
        content()
    }
}
