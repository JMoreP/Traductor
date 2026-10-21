package com.example.traductorlsv.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.traductorlsv.ui.theme.*

/**
 * Diálogo modal estilo Duolingo con base 3D táctil, icono ilustrativo,
 * título con peso visual y botones de confirmación y cancelación.
 */
@Composable
fun DuoDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String = "CANCELAR",
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    iconColor: Color = DuoRed,
    confirmButtonColor: Color = DuoRed,
    confirmButtonDarkColor: Color = DuoRedDark,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        DuoCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            backgroundColor = DuoSurface,
            borderColor = DuoBorder,
            cornerRadius = 24.dp,
            bottomDepth = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    DuoIllustration(
                        icon = icon,
                        color = iconColor,
                        size = 72.dp,
                        iconSizeRatio = 0.55f
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextBlack,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DuoTextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DuoButton(
                        text = confirmText,
                        onClick = onConfirm,
                        backgroundColor = confirmButtonColor,
                        darkShadowColor = confirmButtonDarkColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DuoButton(
                        text = dismissText,
                        onClick = onDismiss,
                        backgroundColor = DuoSurface,
                        darkShadowColor = DuoBorder,
                        textColor = DuoTextGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
