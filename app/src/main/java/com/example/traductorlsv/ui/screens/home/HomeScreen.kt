package com.example.traductorlsv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.ui.components.DuoButton
import com.example.traductorlsv.ui.components.DuoCard
import com.example.traductorlsv.ui.components.DuoIllustration
import com.example.traductorlsv.ui.components.DuoProgressBar
import com.example.traductorlsv.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToTranslator: () -> Unit,
    onNavigateToTrainer: () -> Unit,
    onNavigateToGames: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header superior: Saludo y Chip de Racha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "¡Hola!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextBlack
                )
                Text(
                    text = "¿Listo para practicar LSV hoy?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoTextGray
                )
            }

            // Chip de Racha estilo Duolingo
            DuoCard(
                backgroundColor = DuoSurface,
                borderColor = DuoBorder,
                cornerRadius = 18.dp,
                bottomDepth = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Racha",
                        tint = DuoOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (uiState.streakDays == 1) "1 DÍA" else "${uiState.streakDays} DÍAS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoOrange
                    )
                }
            }
        }

        // 2. Tarjeta de Progreso Diario
        DuoCard(
            backgroundColor = DuoSurface,
            borderColor = DuoBorder,
            cornerRadius = 20.dp,
            bottomDepth = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progreso de hoy",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack
                    )
                    Text(
                        text = "${uiState.todayProgress} / ${uiState.dailyGoal} señas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoTextGray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val safeProgress = if (uiState.dailyGoal > 0) {
                    (uiState.todayProgress.toFloat() / uiState.dailyGoal.toFloat()).coerceIn(0f, 1f)
                } else 0f

                DuoProgressBar(
                    progress = safeProgress,
                    height = 14.dp,
                    progressColor = DuoGreen,
                    trackColor = DuoBorder,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Tarjetas Grandes de Acción
        // Tarjeta 1: Traductor en Vivo (Destacada / Recomendada)
        ActionDashboardCard(
            title = "Traductor en Vivo",
            subtitle = "Reconoce señas en tiempo real",
            icon = Icons.Filled.Videocam,
            iconColor = DuoGreen,
            onClick = onNavigateToTranslator,
            badge = "RECOMENDADO",
            isHighlighted = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tarjeta 2: Entrenador
        ActionDashboardCard(
            title = "Entrenador",
            subtitle = "Gestiona tu vocabulario de señas",
            icon = Icons.Filled.School,
            iconColor = DuoBlue,
            onClick = onNavigateToTrainer
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tarjeta 3: Jugar
        ActionDashboardCard(
            title = "Jugar",
            subtitle = "Aprende jugando y gana estrellas",
            icon = Icons.Filled.SportsEsports,
            iconColor = DuoPurple,
            onClick = onNavigateToGames
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Sección Inferior: Continuar aprendiendo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Continuar aprendiendo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = DuoTextBlack
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TODO(Fase 7): Conectar a la siguiente lección guiada según el progreso del usuario
            DuoButton(
                text = "CONTINUAR LECCIÓN",
                onClick = onNavigateToGames,
                backgroundColor = DuoGreen,
                darkShadowColor = DuoGreenDark,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActionDashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    isHighlighted: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        DuoCard(
            backgroundColor = if (isHighlighted) DuoGreenCardBg.copy(alpha = 0.4f) else DuoSurface,
            borderColor = if (isHighlighted) DuoGreenLight else DuoBorder,
            cornerRadius = 22.dp,
            bottomDepth = 5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ilustración vectorial profesional
                DuoIllustration(
                    icon = icon,
                    color = iconColor,
                    size = 54.dp,
                    iconSizeRatio = 0.55f
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Textos centrales
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DuoGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                color = DuoSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DuoTextGray,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Flecha indicadora '>'
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = DuoTextLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
