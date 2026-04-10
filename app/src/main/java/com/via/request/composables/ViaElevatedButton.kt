package com.via.request.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.via.request.ui.theme.ButtonOutline

@Composable
fun ViaElevatedButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    ElevatedButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, ButtonOutline),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 3.dp
        ),
        contentPadding = PaddingValues(
            vertical = 16.dp,
            horizontal = 24.dp
        ),
        content = {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                content()
            }
        }
    )
}
