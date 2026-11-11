package com.example.traductorlsv.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.R
import com.example.traductorlsv.ui.components.DuoCard
import com.example.traductorlsv.ui.theme.DuoBorder
import com.example.traductorlsv.ui.theme.DuoSurface

/**
 * Pantalla Principal (HomeScreen):
 * - Fondo con degradado vertical turquesa (#22D3F1) que degrada a gris frío suave (#EDF2F7)
 * - Barra superior con pill de días de racha a la izquierda, título "Pasmulio" y campana
 * - Carrusel horizontal (HorizontalPager) con tarjeta turquesa y tarjeta amarilla asomada a la derecha
 * - Sección "Gamificación" unificada dentro de una sola tarjeta blanca contenedora
 */
@Composable
fun HomeScreen(
    onNavigateToTranslator: () -> Unit,
    onNavigateToTrainer: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToStats: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Degradado vertical exacto: inicia en turquesa #22D3F1 y degrada a gris frío suave (#EDF2F7)
    val backgroundGradient = Brush.verticalGradient(
        0.0f to Color(0xFF22D3F1),
        0.26f to Color(0xFF67E8F9).copy(alpha = 0.55f),
        0.48f to Color(0xFFEDF2F7),
        1.0f to Color(0xFFEDF2F7)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(bottom = 115.dp) // Margen para la barra inferior
        ) {
            // 1. BARRA SUPERIOR (Días de racha a la izquierda, Campana a la derecha)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill de Días de Racha (esquina superior izquierda)
                Row(
                    modifier = Modifier
                        .shadow(elevation = 4.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onNavigateToStats() }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Días de racha",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (uiState.streakDays == 1) "1 día" else "${uiState.streakDays} días",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                }

                // Campana de Notificaciones
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToStats() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. CARRUSEL HERO DESLIZABLE
            // Con padding simétrico (20.dp) y pageSpacing (8.dp), la tarjeta turquesa queda
            // 100% centrada (alineada con Gamificación) y 12.dp de la amarilla se asoman a la derecha.
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                pageSpacing = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    // Tarjeta 1: Turquesa (sin height fijo restrictivo para que el botón nunca se corte)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 166.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color(0x3022D3F1),
                                spotColor = Color(0x20000000)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF22D3F1), Color(0xFF06B6D4))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1.25f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Lecciones de\neducación de\ncalidad gratuitas",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    lineHeight = 21.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .shadow(elevation = 3.dp, shape = CircleShape)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFBBF24))
                                        .clickable(onClick = onNavigateToTrainer)
                                        .padding(horizontal = 22.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Comenzar",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(0.75f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.perrito_manchisenas),
                                    contentDescription = "Perrito ManchiSeñas",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(110.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Tarjeta 2: Amarilla (Se asoma 12.dp a la derecha indicando que se puede deslizar)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 166.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color(0x30FBBF24),
                                spotColor = Color(0x20000000)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Practica en vivo\ncon el traductor\nde señas LSV",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                lineHeight = 21.sp
                            )

                            Box(
                                modifier = Modifier
                                    .shadow(elevation = 3.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable(onClick = onNavigateToTranslator)
                                    .padding(horizontal = 22.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Traducir",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. SECCIÓN DE GAMIFICACIÓN UNIFICADA EN UNA SOLA TARJETA CON PROFUNDIDAD 3D (DuoCard)
            DuoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                backgroundColor = DuoSurface,
                borderColor = DuoBorder,
                cornerRadius = 24.dp,
                bottomDepth = 5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Cabecera "Gamificación" + chevron
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToGames),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gamificación",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "Ver más",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Sub-tarjeta 1: Trofeo + desafío diario + botón turquesa "Comenzar"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = "Trofeo",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Completa los desafíos\ndel día para obtener\nmás puntos",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 18.sp
                                )

                                Text(
                                    text = "Progreso de hoy: ${uiState.todayProgress}/${uiState.dailyGoal} señas",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .shadow(elevation = 3.dp, shape = CircleShape)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22D3F1))
                                        .clickable(onClick = onNavigateToGames)
                                        .padding(horizontal = 18.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Comenzar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Sub-tarjeta 2: Retos + botón amarillo "Retos"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Completa los desafíos del día para obtener insignias",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .shadow(elevation = 3.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFBBF24))
                                    .clickable(onClick = onNavigateToGames)
                                    .padding(horizontal = 18.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Retos",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
