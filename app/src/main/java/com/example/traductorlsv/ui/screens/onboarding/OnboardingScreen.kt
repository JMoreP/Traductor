package com.example.traductorlsv.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.ui.components.DuoButton
import com.example.traductorlsv.ui.components.DuoCard
import com.example.traductorlsv.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SignLanguage
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.traductorlsv.ui.components.DuoIllustration

data class OnboardingPageData(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val highlightColor: Color,
    val shadowColor: Color
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            viewModel.completeOnboarding()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OnboardingEvent.NavigateToHome -> onFinished()
            }
        }
    }

    val pages = remember {
        listOf(
            OnboardingPageData(
                icon = Icons.Filled.WavingHand,
                title = "¡Bienvenido a TraductorLSV!",
                description = "La plataforma interactiva para aprender y traducir la Lengua de Señas Venezolana con inteligencia artificial.",
                highlightColor = DuoGreen,
                shadowColor = DuoGreenDark
            ),
            OnboardingPageData(
                icon = Icons.Filled.SignLanguage,
                title = "Aprende el Abecedario",
                description = "Reconoce señas estáticas y dinámicas en tiempo real con algoritmos de visión computacional y retroalimentación instantánea.",
                highlightColor = DuoBlue,
                shadowColor = DuoBlueDark
            ),
            OnboardingPageData(
                icon = Icons.Filled.Star,
                title = "Juega y Gana Estrellas",
                description = "Diviértete con desafíos rápidos, pon a prueba tu memoria visual y supera tus marcas diarias.",
                highlightColor = DuoYellow,
                shadowColor = DuoYellowDark
            ),
            OnboardingPageData(
                icon = Icons.Filled.PhotoCamera,
                title = "Permite la Cámara",
                description = "Para detectar las posturas y movimientos de tus manos con MediaPipe, necesitamos acceso seguro a tu cámara.",
                highlightColor = DuoPurple,
                shadowColor = DuoPurpleDark
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra superior con botón Saltar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicadores de páginas (Dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) DuoGreen else DuoBorder)
                    )
                }
            }

            // Botón Saltar (visible hasta la penúltima página)
            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "SALTAR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextGray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.completeOnboarding() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido deslizable del Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ilustración vectorial profesional estilo Duolingo
                DuoIllustration(
                    icon = page.icon,
                    color = page.highlightColor,
                    size = 140.dp,
                    iconSizeRatio = 0.52f
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = page.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextBlack,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = DuoTextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botones inferiores de acción
        if (isLastPage) {
            DuoButton(
                text = if (hasCameraPermission) "¡EMPEZAR AHORA!" else "HABILITAR CÁMARA Y COMENZAR",
                onClick = {
                    if (hasCameraPermission) {
                        viewModel.completeOnboarding()
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                backgroundColor = DuoGreen,
                darkShadowColor = DuoGreenDark,
                modifier = Modifier.fillMaxWidth()
            )

            if (!hasCameraPermission) {
                Spacer(modifier = Modifier.height(12.dp))
                DuoButton(
                    text = "CONTINUAR POR AHORA",
                    onClick = { viewModel.completeOnboarding() },
                    backgroundColor = DuoSurface,
                    darkShadowColor = DuoBorder,
                    textColor = DuoTextGray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            DuoButton(
                text = "CONTINUAR",
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                backgroundColor = DuoGreen,
                darkShadowColor = DuoGreenDark,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
