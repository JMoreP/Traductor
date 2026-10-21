package com.example.traductorlsv.navigation

/**
 * Definición centralizada de las rutas de navegación para la aplicación TraductorLSV.
 *
 * NOTA DE ARQUITECTURA:
 * En la FASE 1, [Route.Translator] se utiliza como destino inicial TEMPORAL
 * para asegurar que la app conserve su comportamiento inmediato mientras se añade
 * el soporte de navegación.
 *
 * Tras la implementación de la FASE 3, el destino inicial oficial pasará a ser
 * [Route.Home] (Dashboard principal de la app).
 */
sealed class Route(val route: String) {
    object Splash : Route("splash")
    object Onboarding : Route("onboarding")
    object Home : Route("home")
    object Translator : Route("translator") // Destino inicial temporal en Fase 1
    object Trainer : Route("trainer")       // Placeholder para Fase 5
    object Games : Route("games")           // Hub de Juegos (Fase 6)
    object Stats : Route("stats")           // Placeholder para Fase 10
    object Profile : Route("profile")       // Placeholder para Fase 10

    // Rutas de Minijuegos (Fase 6+)
    object GameGuess : Route("game_guess")
    object GameMemory : Route("game_memory")
    object GameSpeed : Route("game_speed")
    object GameChallenge : Route("game_challenge")
}
