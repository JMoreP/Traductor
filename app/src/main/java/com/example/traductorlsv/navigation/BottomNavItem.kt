package com.example.traductorlsv.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconFilled: ImageVector
) {
    object Home : BottomNavItem(Route.Home.route, "Inicio", Icons.Outlined.Home, Icons.Filled.Home)
    object Games : BottomNavItem(Route.Games.route, "Juegos", Icons.Outlined.VideogameAsset, Icons.Filled.VideogameAsset)
    object Stats : BottomNavItem(Route.Stats.route, "Stats", Icons.Outlined.BarChart, Icons.Filled.BarChart)
    object Profile : BottomNavItem(Route.Profile.route, "Perfil", Icons.Outlined.Person, Icons.Filled.Person)

    companion object {
        val all = listOf(Home, Games, Stats, Profile)
    }
}
