package com.example.traductorlsv

import com.example.traductorlsv.navigation.BottomNavItem
import com.example.traductorlsv.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {

    @Test
    fun verify_routes_are_defined_and_not_empty() {
        assertEquals("splash", Route.Splash.route)
        assertEquals("onboarding", Route.Onboarding.route)
        assertEquals("home", Route.Home.route)
        assertEquals("translator", Route.Translator.route)
        assertEquals("trainer", Route.Trainer.route)
        assertEquals("games", Route.Games.route)
        assertEquals("stats", Route.Stats.route)
        assertEquals("profile", Route.Profile.route)
    }

    @Test
    fun verify_splash_is_unique_from_home_and_onboarding() {
        assertNotNull(Route.Splash.route)
        assert(Route.Splash.route != Route.Home.route)
        assert(Route.Splash.route != Route.Onboarding.route)
    }

    @Test
    fun verify_bottom_nav_items_count_and_routes() {
        assertEquals(4, BottomNavItem.all.size)
        val routes = BottomNavItem.all.map { it.route }
        assertTrue(routes.contains(Route.Home.route))
        assertTrue(routes.contains(Route.Games.route))
        assertTrue(routes.contains(Route.Stats.route))
        assertTrue(routes.contains(Route.Profile.route))
    }
}
