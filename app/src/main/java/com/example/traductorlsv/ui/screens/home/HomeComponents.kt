package com.example.traductorlsv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traductorlsv.ui.components.ModernCard
import com.example.traductorlsv.ui.theme.*

/**
 * Botón circular / pill con sombra 3D suave para la fila de acciones rápidas.
 */
@Composable
fun HomeQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    accentColor: Color = DuoTurquoise
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        // Contenedor circular / squircle con sombra flotante 3D
        Box(
            modifier = Modifier
                .size(62.dp)
                .shadow(
                    elevation = if (isHighlighted) 10.dp else 6.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = if (isHighlighted) accentColor.copy(alpha = 0.35f) else Color(0x15000000),
                    spotColor = if (isHighlighted) accentColor.copy(alpha = 0.5f) else Color(0x24000000)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(if (isHighlighted) accentColor else DuoSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isHighlighted) DuoSurface else accentColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DuoTextBlack
        )
    }
}

/**
 * Tarjeta de Gamificación grande con sombra 3D, ícono de trofeo y métricas de racha.
 */
@Composable
fun GamificationCard(
    streakDays: Int,
    todayProgress: Int,
    dailyGoal: Int,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = 8.dp,
        shadowColor = Color(0x1F09E1FB)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor dorado/amarillo con el Trofeo grande
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF9E5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = "Trofeo de Gamificación",
                    tint = DuoYellow,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos descriptivos y botón de acción
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "¡Racha Absoluta!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack
                    )
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = DuoOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (streakDays == 1) "1 día consecutivo" else "$streakDays días consecutivos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoOrange
                )

                Text(
                    text = "Progreso de hoy: $todayProgress / $dailyGoal señas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = DuoTextGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Botón turquesa "Continuar"
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            spotColor = DuoTurquoiseDark
                        )
                        .clip(CircleShape)
                        .background(DuoTurquoise)
                        .clickable(onClick = onContinueClick)
                        .padding(horizontal = 18.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continuar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoSurface
                    )
                }
            }
        }
    }
}
