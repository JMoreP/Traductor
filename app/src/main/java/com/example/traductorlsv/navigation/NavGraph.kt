package com.example.traductorlsv.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.traductorlsv.ui.components.DuoBottomNavigation
import com.example.traductorlsv.ui.screens.games.Game
import com.example.traductorlsv.ui.screens.games.GamePlaceholderScreen
import com.example.traductorlsv.ui.screens.games.GamesScreen
import com.example.traductorlsv.ui.screens.games.guess.GuessGameScreen
import com.example.traductorlsv.ui.screens.games.memory.MemoryGameScreen
import com.example.traductorlsv.ui.screens.games.speed.SpeedGameScreen
import com.example.traductorlsv.ui.screens.home.HomeScreen
import com.example.traductorlsv.ui.screens.onboarding.OnboardingScreen
import com.example.traductorlsv.ui.screens.profile.ProfileScreen
import com.example.traductorlsv.ui.screens.splash.SplashScreen
import com.example.traductorlsv.ui.screens.stats.StatsScreen
import com.example.traductorlsv.ui.screens.trainer.TrainerScreen
import com.example.traductorlsv.ui.screens.translator.TranslatorScreen

@Composable
fun TraductorNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas principales donde la barra de navegación inferior DEBE mostrarse
    val bottomBarRoutes = remember {
        setOf(
            Route.Home.route,
            Route.Games.route,
            Route.Stats.route,
            Route.Profile.route,
            Route.GameGuess.route,
            Route.GameMemory.route,
            Route.GameSpeed.route,
            Route.GameChallenge.route
        )
    }
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                val selectedBottomRoute = when (currentRoute) {
                    Route.GameGuess.route,
                    Route.GameMemory.route,
                    Route.GameSpeed.route,
                    Route.GameChallenge.route -> Route.Games.route
                    else -> currentRoute
                }
                DuoBottomNavigation(
                    currentRoute = selectedBottomRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Flujo Inicial
            composable(Route.Splash.route) {
                SplashScreen(
                    onNavigate = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(Route.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Route.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Route.Home.route) {
                            popUpTo(Route.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Pantallas Principales (con BottomBar)
            composable(Route.Home.route) {
                HomeScreen(
                    onNavigateToTranslator = {
                        navController.navigate(Route.Translator.route)
                    },
                    onNavigateToTrainer = {
                        navController.navigate(Route.Trainer.route)
                    },
                    onNavigateToGames = {
                        navController.navigate(Route.Games.route)
                    }
                )
            }

            composable(Route.Games.route) {
                GamesScreen(
                    onNavigateToGame = { gameRoute ->
                        navController.navigate(gameRoute)
                    },
                    onNavigateToTrainer = {
                        navController.navigate(Route.Trainer.route)
                    }
                )
            }

            composable(Route.GameGuess.route) {
                GuessGameScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToTrainer = {
                        navController.navigate(Route.Trainer.route)
                    }
                )
            }

            composable(Route.GameMemory.route) {
                MemoryGameScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToTrainer = {
                        navController.navigate(Route.Trainer.route)
                    }
                )
            }

            composable(Route.GameSpeed.route) {
                SpeedGameScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToTrainer = {
                        navController.navigate(Route.Trainer.route)
                    }
                )
            }

            composable(Route.GameChallenge.route) {
                GamePlaceholderScreen(
                    game = Game.Challenge,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Route.Stats.route) {
                StatsScreen()
            }

            composable(Route.Profile.route) {
                ProfileScreen()
            }

            // Módulos Especializados (sin BottomBar)
            composable(Route.Translator.route) {
                TranslatorScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Route.Trainer.route) {
                TrainerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
