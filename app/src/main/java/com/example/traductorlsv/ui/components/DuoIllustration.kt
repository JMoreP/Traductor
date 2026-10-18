package com.example.traductorlsv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.traductorlsv.ui.theme.DuoGreen

/**
 * Componente visual reutilizable estilo Duolingo que combina un contenedor circular
 * tonal con borde suave e icono vectorial centralizado.
 */
@Composable
fun DuoIllustration(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = DuoGreen,
    size: Dp = 96.dp,
    iconSizeRatio: Float = 0.5f,
    contentDescription: String? = null
) {
    val iconSize = size * iconSizeRatio

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .border(2.dp, color.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}
