package com.example.traductorlsv.ui.screens.games

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
import androidx.compose.material.icons.filled.SportsEsports
import com.example.traductorlsv.ui.components.DuoCard
import com.example.traductorlsv.ui.components.DuoIllustration
import com.example.traductorlsv.ui.theme.*

@Composable
fun GamesScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título de la sección
        Text(
            text = "Centro de Juegos",
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
                    icon = Icons.Filled.SportsEsports,
                    color = DuoPurple,
                    size = 110.dp,
                    iconSizeRatio = 0.55f
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Juegos de Práctica",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextBlack,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¡Próximamente en Fase 6!\nDesafíos rápidos, juego de memoria visual y adivinanza de señas para poner a prueba tu destreza.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = DuoTextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
