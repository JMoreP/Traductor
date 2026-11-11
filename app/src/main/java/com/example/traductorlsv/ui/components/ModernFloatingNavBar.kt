package com.example.traductorlsv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.traductorlsv.navigation.Route
import com.example.traductorlsv.ui.theme.DuoTurquoise
import com.example.traductorlsv.ui.theme.DuoTurquoiseDark

/**
 * Construye el contorno superior curvo con cuna central para compartir la geometría exacta
 * entre el recorte de la barra, su sombra superior y su borde.
 */
private fun buildCradleTopContourPath(
    size: Size,
    cradleRadiusPx: Float,
    cornerRadiusPx: Float
): Path {
    val cx = size.width / 2f
    val cradleDepth = cradleRadiusPx * 0.92f
    val cradleSpread = cradleRadiusPx * 1.4f

    return Path().apply {
        moveTo(0f, cornerRadiusPx)
        quadraticTo(0f, 0f, cornerRadiusPx, 0f)
        lineTo(cx - cradleSpread, 0f)

        cubicTo(
            cx - cradleRadiusPx * 0.72f, 0f,
            cx - cradleRadiusPx * 0.72f, cradleDepth,
            cx, cradleDepth
        )
        cubicTo(
            cx + cradleRadiusPx * 0.72f, cradleDepth,
            cx + cradleRadiusPx * 0.72f, 0f,
            cx + cradleSpread, 0f
        )

        lineTo(size.width - cornerRadiusPx, 0f)
        quadraticTo(size.width, 0f, size.width, cornerRadiusPx)
    }
}

private fun buildCradleFullPath(
    size: Size,
    cradleRadiusPx: Float,
    cornerRadiusPx: Float
): Path {
    return buildCradleTopContourPath(size, cradleRadiusPx, cornerRadiusPx).apply {
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
}

/**
 * Forma con cuna circular curva en el centro superior (Cradle Shape),
 * permitiendo que el botón circular FAB descanse armoniosamente sobre la barra blanca.
 */
class BottomNavCradleShape(
    private val cradleRadius: Dp = 38.dp,
    private val cornerRadius: Dp = 28.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cradleR = with(density) { cradleRadius.toPx() }
        val cornerR = with(density) { cornerRadius.toPx() }
        return Outline.Generic(buildCradleFullPath(size, cradleR, cornerR))
    }
}

/**
 * Barra de navegación inferior idéntica a la imagen de referencia:
 * - Fondo blanco puro (#FFFFFF) que contrasta con el fondo gris frío (#EDF2F7) mediante
 *   una sombra difusa proyectada hacia arriba y un borde superior sutil (#CBD5E1).
 * - Iconos ampliados a 30dp para fácil pulsación y alta legibilidad:
 *   Activo en turquesa (#22D3F1) e inactivos en gris pizarra (#94A3B8).
 * - Botón central circular turquesa (#22D3F1) de 62dp con halo celeste suave (#99EEFD)
 *   y símbolo gestual oscuro (#0F172A).
 */
@Composable
fun ModernFloatingNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    activeColor: Color = DuoTurquoise,
    inactiveColor: Color = Color(0xFF94A3B8)
) {
    val cradleRadius = 38.dp
    val cornerRadius = 28.dp
    val cradleShape = remember { BottomNavCradleShape(cradleRadius = cradleRadius, cornerRadius = cornerRadius) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Barra blanca con sombra proyectada hacia arriba y borde superior sutil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val cradleR = cradleRadius.toPx()
                    val cornerR = cornerRadius.toPx()
                    val topContour = buildCradleTopContourPath(size, cradleR, cornerR)
                    val fullPath = buildCradleFullPath(size, cradleR, cornerR)

                    // Sombra suave proyectada hacia afuera/arriba sin ensuciar el blanco interior
                    clipPath(fullPath, clipOp = ClipOp.Difference) {
                        val shadowColor = Color(0xFF0F172A)
                        val layers = listOf(
                            18.dp to 0.025f,
                            12.dp to 0.04f,
                            6.dp to 0.055f,
                            2.5.dp to 0.07f
                        )
                        layers.forEach { (strokeWidth, alpha) ->
                            drawPath(
                                path = topContour,
                                color = shadowColor.copy(alpha = alpha),
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Relleno blanco puro de la barra
                    drawPath(path = fullPath, color = backgroundColor)

                    // Borde fino superior para definición nítida sobre fondos claros
                    drawPath(
                        path = topContour,
                        color = Color(0xFFCBD5E1).copy(alpha = 0.7f),
                        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                .clip(cradleShape)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home (Inicio) - Icono ampliado a 30dp
                NavBarIconItem(
                    selected = currentRoute == Route.Home.route,
                    iconOutlined = Icons.Outlined.Home,
                    iconFilled = Icons.Filled.Home,
                    contentDescription = "Inicio",
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onNavigate(Route.Home.route) }
                )

                // 2. Explorar / Brújula (Juegos) - Icono ampliado a 30dp
                NavBarIconItem(
                    selected = currentRoute == Route.Games.route,
                    iconOutlined = Icons.Outlined.Explore,
                    iconFilled = Icons.Filled.Explore,
                    contentDescription = "Explorar Juegos",
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onNavigate(Route.Games.route) }
                )

                // 3. Espacio reservado para la cuna del botón central
                Spacer(modifier = Modifier.size(62.dp))

                // 4. Campana (Estadísticas / Avisos) - Icono ampliado a 30dp
                NavBarIconItem(
                    selected = currentRoute == Route.Stats.route,
                    iconOutlined = Icons.Outlined.Notifications,
                    iconFilled = Icons.Filled.Notifications,
                    contentDescription = "Estadísticas y Notificaciones",
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onNavigate(Route.Stats.route) }
                )

                // 5. Usuario (Perfil) - Icono ampliado a 30dp
                NavBarIconItem(
                    selected = currentRoute == Route.Profile.route,
                    iconOutlined = Icons.Outlined.Person,
                    iconFilled = Icons.Filled.Person,
                    contentDescription = "Perfil",
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onNavigate(Route.Profile.route) }
                )
            }
        }

        // 2. Botón Central Elevado (Turquesa #22D3F1 con halo celeste suave #99EEFD)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
                .size(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = DuoTurquoise.copy(alpha = 0.55f),
                    spotColor = DuoTurquoiseDark
                )
                .clip(CircleShape)
                .background(Color(0xFF99EEFD)) // Halo celeste claro exterior de la imagen
                .padding(4.dp)
                .clip(CircleShape)
                .background(DuoTurquoise)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onNavigate(Route.Translator.route) }
                ),
            contentAlignment = Alignment.Center
        ) {
            GestureWaveSymbol(
                color = Color(0xFF0F172A),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Trazo gestual fluido presente en el botón central de la referencia.
 */
@Composable
private fun GestureWaveSymbol(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 2.8.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.52f, size.height * 0.20f)
            cubicTo(
                size.width * 0.24f, size.height * 0.35f,
                size.width * 0.76f, size.height * 0.52f,
                size.width * 0.38f, size.height * 0.66f
            )
            cubicTo(
                size.width * 0.20f, size.height * 0.77f,
                size.width * 0.66f, size.height * 0.86f,
                size.width * 0.48f, size.height * 0.90f
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun NavBarIconItem(
    selected: Boolean,
    iconOutlined: ImageVector,
    iconFilled: ImageVector,
    contentDescription: String,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tintColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "navIconTint"
    )

    Box(
        modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (selected) iconFilled else iconOutlined,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(30.dp)
        )
    }
}
