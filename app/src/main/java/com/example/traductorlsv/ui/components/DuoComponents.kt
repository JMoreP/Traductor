package com.example.traductorlsv.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traductorlsv.ui.theme.*

/**
 * Botón 3D táctil con relieve y animación de hundimiento al presionar,
 * característico del sistema de diseño de Duolingo.
 */
@Composable
fun DuoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DuoGreen,
    darkShadowColor: Color = DuoGreenDark,
    textColor: Color = DuoSurface,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    cornerRadius: Dp = 16.dp,
    bottomDepth: Dp = 4.dp,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val actualBg = if (enabled) backgroundColor else DuoGrayButton
    val actualDark = if (enabled) darkShadowColor else DuoGrayButtonDark
    val actualText = if (enabled) textColor else DuoTextLight

    // Animación de pulsación (el botón se hunde 3dp)
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) bottomDepth - 1.dp else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "DuoButtonPress"
    )

    Box(
        modifier = modifier
            .height(height)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Capa inferior: Sombra sólida 3D
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(cornerRadius))
                .background(actualDark)
        )

        // Capa superior: Frente del botón que se desplaza hacia abajo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height - bottomDepth)
                .offset(y = pressOffset)
                .clip(RoundedCornerShape(cornerRadius))
                .background(actualBg),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(),
                    color = actualText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Tarjeta limpia con borde redondeado y base 3D estilo Duolingo.
 */
@Composable
fun DuoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DuoSurface,
    borderColor: Color = DuoBorder,
    cornerRadius: Dp = 20.dp,
    bottomDepth: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(DuoCardBottom)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomDepth)
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .border(2.dp, borderColor, RoundedCornerShape(cornerRadius))
                .padding(14.dp)
        ) {
            Column(content = content)
        }
    }
}

/**
 * Chip de estadísticas redondeado con icono y texto en negrita.
 */
@Composable
fun DuoChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = DuoTextBlack,
    backgroundColor: Color = DuoSurface,
    borderColor: Color = DuoBorder,
    textColor: Color = DuoTextBlack
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}

/**
 * Switch segmentado estilo Duolingo para alternar modos.
 */
@Composable
fun DuoSegmentedToggle(
    isTrainingMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DuoBgLight)
            .border(2.dp, DuoBorder, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        // Opción 1: Traducir
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (!isTrainingMode) DuoBlue else Color.Transparent)
                .clickable { onModeChange(false) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TRADUCIR",
                color = if (!isTrainingMode) DuoSurface else DuoTextGray,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.width(4.dp))

        // Opción 2: Entrenar
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isTrainingMode) DuoGreen else Color.Transparent)
                .clickable { onModeChange(true) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ENTRENAR",
                color = if (isTrainingMode) DuoSurface else DuoTextGray,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Campo de texto estilo Duolingo con bordes suaves y tipografía contundente.
 */
@Composable
fun DuoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DuoBgLight)
            .border(2.dp, DuoBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = DuoTextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = DuoTextBlack,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            ),
            cursorBrush = SolidColor(DuoBlue),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Banner de alerta o estado animado con colores pasteles/vibrantes de Duolingo.
 */
@Composable
fun DuoBanner(
    message: String,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isRecording -> DuoYellowCardBg
        message.contains("Borrados", ignoreCase = true) || message.contains("No hallado", ignoreCase = true) -> DuoRedCardBg
        else -> DuoGreenCardBg
    }

    val textColor = when {
        isRecording -> DuoYellowDark
        message.contains("Borrados", ignoreCase = true) || message.contains("No hallado", ignoreCase = true) -> DuoRedDark
        else -> DuoGreenDark
    }

    val borderColor = when {
        isRecording -> DuoYellow
        message.contains("Borrados", ignoreCase = true) || message.contains("No hallado", ignoreCase = true) -> DuoRed
        else -> DuoGreen
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        val iconVector = when {
            isRecording -> Icons.Filled.Timer
            message.contains("Borrados", ignoreCase = true) -> Icons.Filled.Delete
            message.contains("Escribe", ignoreCase = true) -> Icons.Filled.Warning
            else -> Icons.Filled.CheckCircle
        }
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Barra de progreso redondeada estilo Duolingo con track de fondo y barra animada.
 */
@Composable
fun DuoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    progressColor: Color = DuoGreen,
    trackColor: Color = DuoBorder,
    cornerRadius: Dp = 10.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "DuoProgressBar"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(trackColor)
    ) {
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(progressColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height / 3)
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(Color.White.copy(alpha = 0.35f))
                )
            }
        }
    }
}

