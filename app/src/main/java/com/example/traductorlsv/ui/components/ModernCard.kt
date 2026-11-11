package com.example.traductorlsv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.traductorlsv.ui.theme.DuoBorder
import com.example.traductorlsv.ui.theme.DuoSurface

/**
 * Tarjeta moderna con bordes redondeados (24dp) y sombra flotante suave,
 * alineada con la estética Neumorfismo / Flat 3D del rediseño.
 */
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = DuoSurface,
    borderColor: Color = DuoBorder.copy(alpha = 0.6f),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    shadowColor: Color = Color(0x1A09E1FB),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = Color(0x24000000)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .then(clickModifier)
    ) {
        content()
    }
}
