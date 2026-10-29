package com.example.traductorlsv.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.ui.components.*
import com.example.traductorlsv.ui.theme.*

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Estados para diálogos
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var showResetProgressDialog by remember { mutableStateOf(false) }
    var showClearDatasetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
    ) {
        // TopBar institucional
        DuoTopBar(
            title = "Perfil",
            onBackClick = null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Cabecera del Usuario (Avatar, Nombre y Barra de XP/Nivel)
            UserProfileHeaderCard(
                userName = uiState.userName,
                level = uiState.level,
                currentXp = uiState.currentLevelXp,
                nextLevelXp = uiState.nextLevelXp,
                totalXp = uiState.xp,
                progress = uiState.levelProgress,
                onEditNameClick = {
                    tempName = uiState.userName
                    showEditNameDialog = true
                }
            )

            // Ajustes de Aplicación (Sonido y Vibración)
            SettingsSection(
                soundEnabled = uiState.soundEnabled,
                vibrationEnabled = uiState.vibrationEnabled,
                onSoundToggle = { viewModel.setSoundEnabled(it) },
                onVibrationToggle = { viewModel.setVibrationEnabled(it) }
            )

            // Zona de Peligro (Reiniciar Progreso / Borrar Vocabulario)
            DangerZoneSection(
                onResetProgressClick = { showResetProgressDialog = true },
                onClearDatasetClick = { showClearDatasetDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo para editar nombre de usuario
    if (showEditNameDialog) {
        EditNameDialog(
            currentName = tempName,
            onNameChange = { tempName = it },
            onConfirm = {
                viewModel.updateUserName(tempName)
                showEditNameDialog = false
            },
            onDismiss = { showEditNameDialog = false }
        )
    }

    // Diálogo de confirmación: Reiniciar Progreso
    if (showResetProgressDialog) {
        DuoDialog(
            title = "¿Reiniciar progreso?",
            message = "Se reiniciarán tus rachas, estadísticas y partidas jugadas. Tu vocabulario de señas entrenadas NO se borrará.",
            confirmText = "REINICIAR",
            onConfirm = {
                viewModel.resetProgress()
                showResetProgressDialog = false
            },
            dismissText = "CANCELAR",
            onDismiss = { showResetProgressDialog = false },
            icon = Icons.Rounded.Refresh,
            iconColor = DuoRed,
            confirmButtonColor = DuoRed,
            confirmButtonDarkColor = DuoRedDark
        )
    }

    // Diálogo de confirmación: Borrar Dataset
    if (showClearDatasetDialog) {
        DuoDialog(
            title = "¿Borrar vocabulario?",
            message = "Se eliminarán todas las señas entrenadas en tu vocabulario. El traductor dejará de reconocerlas. Tus rachas y estadísticas NO se borrarán.",
            confirmText = "BORRAR SEÑAS",
            onConfirm = {
                viewModel.clearDataset()
                showClearDatasetDialog = false
            },
            dismissText = "CANCELAR",
            onDismiss = { showClearDatasetDialog = false },
            icon = Icons.Rounded.DeleteForever,
            iconColor = DuoRed,
            confirmButtonColor = DuoRed,
            confirmButtonDarkColor = DuoRedDark
        )
    }
}

/**
 * TopBar para pantallas de navegación primaria.
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
 * Tarjeta superior con avatar, nombre editable y progreso de nivel (XP).
 */
@Composable
private fun UserProfileHeaderCard(
    userName: String,
    level: Int,
    currentXp: Int,
    nextLevelXp: Int,
    totalXp: Int,
    progress: Float,
    onEditNameClick: () -> Unit
) {
    DuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = DuoSurface,
        borderColor = DuoBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DuoIllustration(
                icon = Icons.Rounded.Person,
                color = DuoBlue,
                size = 72.dp,
                iconSizeRatio = 0.55f
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onEditNameClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar nombre",
                            tint = DuoBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DuoGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NIVEL $level",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoGreenDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$totalXp XP acumulados",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoTextGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de experiencia hacia el siguiente nivel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progreso del Nivel $level",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DuoTextGray
            )
            Text(
                text = "$currentXp / $nextLevelXp XP",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = DuoTextBlack
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        DuoProgressBar(
            progress = progress,
            progressColor = DuoGreen,
            height = 12.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Sección de Ajustes (Sonido y Vibración).
 */
@Composable
private fun SettingsSection(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "PREFERENCIAS",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextGray,
            letterSpacing = 0.5.sp
        )

        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DuoSurface,
            borderColor = DuoBorder
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fila de Sonido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = DuoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Efectos de sonido",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoTextBlack
                            )
                            Text(
                                text = "Sonidos al acertar o fallar señas",
                                fontSize = 12.sp,
                                color = DuoTextGray
                            )
                        }
                    }

                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DuoSurface,
                            checkedTrackColor = DuoGreen,
                            uncheckedThumbColor = DuoSurface,
                            uncheckedTrackColor = DuoBorder
                        )
                    )
                }

                HorizontalDivider(color = DuoBorder.copy(alpha = 0.5f), thickness = 1.dp)

                // Fila de Vibración
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint = DuoOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vibración háptica",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoTextBlack
                            )
                            Text(
                                text = "Respuesta táctil durante el juego",
                                fontSize = 12.sp,
                                color = DuoTextGray
                            )
                        }
                    }

                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = onVibrationToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DuoSurface,
                            checkedTrackColor = DuoGreen,
                            uncheckedThumbColor = DuoSurface,
                            uncheckedTrackColor = DuoBorder
                        )
                    )
                }
            }
        }
    }
}

/**
 * Sección Zona de Peligro para reseteos controlados.
 */
@Composable
private fun DangerZoneSection(
    onResetProgressClick: () -> Unit,
    onClearDatasetClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = DuoRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ZONA DE PELIGRO",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = DuoRed,
                letterSpacing = 0.5.sp
            )
        }

        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DuoSurface,
            borderColor = DuoRed.copy(alpha = 0.4f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Opción 1: Reiniciar Progreso
                Column {
                    Text(
                        text = "Reiniciar Progreso",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Limpia tus estadísticas, rachas y récords sin tocar las señas guardadas.",
                        fontSize = 12.sp,
                        color = DuoTextGray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DuoButton(
                        text = "REINICIAR PROGRESO",
                        onClick = onResetProgressClick,
                        backgroundColor = DuoRed,
                        darkShadowColor = DuoRedDark,
                        height = 44.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = DuoBorder.copy(alpha = 0.6f), thickness = 1.dp)

                // Opción 2: Borrar Vocabulario
                Column {
                    Text(
                        text = "Borrar Vocabulario (Dataset)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Elimina todas las muestras de señas entrenadas. Mantiene tus rachas.",
                        fontSize = 12.sp,
                        color = DuoTextGray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DuoButton(
                        text = "BORRAR DATASET",
                        onClick = onClearDatasetClick,
                        backgroundColor = DuoRed,
                        darkShadowColor = DuoRedDark,
                        height = 44.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Diálogo modal para editar el nombre de usuario.
 */
@Composable
private fun EditNameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DuoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            backgroundColor = DuoSurface,
            borderColor = DuoBorder,
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EDITAR TU NOMBRE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextBlack
                )

                Spacer(modifier = Modifier.height(14.dp))

                DuoTextField(
                    value = currentName,
                    onValueChange = onNameChange,
                    placeholder = "Escribe tu nombre...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DuoButton(
                        text = "CANCELAR",
                        onClick = onDismiss,
                        backgroundColor = DuoGrayButton,
                        darkShadowColor = DuoGrayButtonDark,
                        textColor = DuoTextLight,
                        height = 46.dp,
                        modifier = Modifier.weight(1f)
                    )
                    DuoButton(
                        text = "GUARDAR",
                        onClick = onConfirm,
                        backgroundColor = DuoGreen,
                        darkShadowColor = DuoGreenDark,
                        height = 46.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
