package com.example.traductorlsv.ui.screens.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.R
import com.example.traductorlsv.ui.components.DuoCard
import com.example.traductorlsv.ui.components.DuoIllustration
import com.example.traductorlsv.ui.theme.*

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
    ) {
        // TopBar institucional estilo Duolingo
        DuoTopBar(
            title = "Estadísticas",
            onBackClick = null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header con Mascota Turpial
            StatsMascotHeader()

            // Grid 2x2 de Resumen General
            StatsSummaryGrid(
                currentStreak = uiState.currentStreak,
                longestStreak = uiState.longestStreak,
                totalSigns = uiState.totalSignsLearned,
                totalGames = uiState.totalGamesPlayed
            )

            // Gráfico semanal de actividad (Últimos 7 días)
            WeeklyActivityChart(
                daysActivity = uiState.last7DaysActivity,
                maxCount = uiState.maxWeeklyCount
            )

            // Sección de Logros
            AchievementsSection(
                achievements = uiState.achievements
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * TopBar con soporte para pantallas de primer nivel (sin botón atrás).
 */
@Composable
private fun DuoTopBar(
    title: String,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .drawBehind {
                drawLine(
                    color = DuoBorder,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .background(DuoSurface)
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = DuoTextBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Header superior con la mascota oficial Turpi y mensaje motivador.
 */
@Composable
private fun StatsMascotHeader() {
    DuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = DuoSurface,
        borderColor = DuoBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Intentar cargar la mascota desde drawable, o usar ilustración fallback
            val context = LocalContext.current
            val hasMascotDrawable = context.resources.getIdentifier("mascot_happy", "drawable", context.packageName) != 0

            if (hasMascotDrawable) {
                Image(
                    painter = painterResource(id = R.drawable.mascot_happy),
                    contentDescription = "Mascota Turpi",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(80.dp)
                )
            } else {
                DuoIllustration(
                    icon = Icons.Rounded.BarChart,
                    color = DuoGreen,
                    size = 64.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¡TU PROGRESO LSV!",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoGreenDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cada seña y partida te acerca a dominar la comunicación.",
                    fontSize = 13.sp,
                    color = DuoTextGray,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

/**
 * Grid 2x2 de métricas principales.
 */
@Composable
private fun StatsSummaryGrid(
    currentStreak: Int,
    longestStreak: Int,
    totalSigns: Int,
    totalGames: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Rounded.LocalFireDepartment,
                iconColor = DuoOrange,
                value = "$currentStreak",
                unit = if (currentStreak == 1) "DÍA" else "DÍAS",
                title = "Racha actual",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Rounded.EmojiEvents,
                iconColor = DuoYellow,
                value = "$longestStreak",
                unit = if (longestStreak == 1) "DÍA" else "DÍAS",
                title = "Mejor racha",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Rounded.School,
                iconColor = DuoBlue,
                value = "$totalSigns",
                unit = "SEÑAS",
                title = "Vocabulario",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Rounded.SportsEsports,
                iconColor = DuoGreen,
                value = "$totalGames",
                unit = "PARTIDAS",
                title = "Jugadas",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    unit: String,
    title: String,
    modifier: Modifier = Modifier
) {
    DuoCard(
        modifier = modifier,
        backgroundColor = DuoSurface,
        borderColor = DuoBorder
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            DuoIllustration(
                icon = icon,
                color = iconColor,
                size = 46.dp,
                iconSizeRatio = 0.55f
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = iconColor,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoTextGray
                )
            }
        }
    }
}

/**
 * Gráfico de barras de actividad para los últimos 7 días.
 */
@Composable
private fun WeeklyActivityChart(
    daysActivity: List<DayActivity>,
    maxCount: Int
) {
    DuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = DuoSurface,
        borderColor = DuoBorder
    ) {
        Text(
            text = "ACTIVIDAD ESTA SEMANA",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack
        )
        Text(
            text = "Traducciones y partidas completadas",
            fontSize = 12.sp,
            color = DuoTextGray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Contenedor del gráfico de 7 columnas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            daysActivity.forEach { day ->
                DayBarColumn(
                    day = day,
                    maxCount = maxCount,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DayBarColumn(
    day: DayActivity,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val barHeightRatio = if (maxCount > 0 && day.count > 0) {
        (day.count.toFloat() / maxCount.toFloat()).coerceIn(0.12f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Conteo sobre la barra
        if (day.count > 0) {
            Text(
                text = "${day.count}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (day.isToday) DuoGreenDark else DuoTextGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Barra gráfica vertical
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(76.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DuoBorder.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (barHeightRatio > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(barHeightRatio)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (day.isToday) DuoGreen else DuoGreen.copy(alpha = 0.85f))
                )
            } else {
                // Barra mínima para días sin actividad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DuoBorder)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre del día
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (day.isToday) DuoGreen.copy(alpha = 0.15f) else Color.Transparent)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = day.dayOfWeek,
                fontSize = 11.sp,
                fontWeight = if (day.isToday) FontWeight.Black else FontWeight.Bold,
                color = if (day.isToday) DuoGreenDark else DuoTextGray
            )
        }
    }
}

/**
 * Sección de Logros obtenidos y por desbloquear.
 */
@Composable
private fun AchievementsSection(
    achievements: List<AchievementUi>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TUS LOGROS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = DuoTextBlack
            )
            val unlockedCount = achievements.count { it.isUnlocked }
            Text(
                text = "$unlockedCount / ${achievements.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = DuoGreenDark
            )
        }

        achievements.forEach { achievement ->
            AchievementItemCard(achievement = achievement)
        }
    }
}

@Composable
private fun AchievementItemCard(
    achievement: AchievementUi
) {
    val icon = when (achievement.iconName) {
        "School" -> Icons.Rounded.School
        "SportsEsports" -> Icons.Rounded.SportsEsports
        "LocalFireDepartment" -> Icons.Rounded.LocalFireDepartment
        "Bolt" -> Icons.Rounded.Star
        "Psychology" -> Icons.Rounded.Psychology
        else -> Icons.Rounded.EmojiEvents
    }

    val iconColor = when (achievement.iconName) {
        "School" -> DuoBlue
        "SportsEsports" -> DuoGreen
        "LocalFireDepartment" -> DuoOrange
        "Bolt" -> DuoYellow
        "Psychology" -> DuoPurple
        else -> DuoGreen
    }

    DuoCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!achievement.isUnlocked) Modifier.alpha(0.55f) else Modifier),
        backgroundColor = DuoSurface,
        borderColor = if (achievement.isUnlocked) iconColor.copy(alpha = 0.4f) else DuoBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                DuoIllustration(
                    icon = icon,
                    color = if (achievement.isUnlocked) iconColor else DuoGrayButton,
                    size = 52.dp,
                    iconSizeRatio = 0.55f
                )
                if (!achievement.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(DuoTextBlack.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = achievement.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (achievement.isUnlocked) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Completado",
                            tint = DuoGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    color = DuoTextGray,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
