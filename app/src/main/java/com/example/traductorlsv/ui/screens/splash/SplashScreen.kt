package com.example.traductorlsv.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignLanguage
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.traductorlsv.ui.components.DuoIllustration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.ui.theme.DuoGreen
import com.example.traductorlsv.ui.theme.DuoGreenDark
import com.example.traductorlsv.ui.theme.DuoSurface
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = viewModel()
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is SplashNavigationEvent.NavigateTo -> onNavigate(event.route)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DuoGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
        ) {
            // Icono central vectorial profesional estilo Duolingo
            DuoIllustration(
                icon = Icons.Filled.SignLanguage,
                color = DuoGreen,
                size = 120.dp,
                iconSizeRatio = 0.55f,
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .background(DuoSurface, CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TraductorLSV",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = DuoSurface,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lengua de Señas Venezolana",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DuoSurface.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }

        // Indicador inferior
        Text(
            text = "Aprende y Traduce",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DuoGreenDark,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}
