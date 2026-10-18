package com.example.traductorlsv.ui.screens.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import com.example.traductorlsv.ui.components.DuoButton
import com.example.traductorlsv.ui.components.DuoCard
import com.example.traductorlsv.ui.components.DuoIllustration
import com.example.traductorlsv.ui.theme.*

@Composable
fun TrainerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Entrenador de Señas",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        DuoCard(
            backgroundColor = DuoSurface,
            borderColor = DuoBorder,
            cornerRadius = 24.dp,
            bottomDepth = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DuoIllustration(
                    icon = Icons.Filled.School,
                    color = DuoBlue,
                    size = 110.dp,
                    iconSizeRatio = 0.55f
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Creador de Dataset",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextBlack,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¡Disponible en Fase 5!\nGraba muestras para nuevas señas, gestiona tu propio dataset de señas personalizadas y calibra los umbrales KNN/DTW.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = DuoTextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                DuoButton(
                    text = "VOLVER AL INICIO",
                    onClick = onNavigateBack,
                    backgroundColor = DuoBlue,
                    darkShadowColor = DuoBlueDark,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
