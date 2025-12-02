package com.tecsup.eventplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.tecsup.eventplanner.ui.screens.EventEntryScreen
import com.tecsup.eventplanner.ui.screens.HomeScreen
import com.tecsup.eventplanner.ui.screens.LoginScreen
import com.tecsup.eventplanner.ui.screens.RegisterScreen

object Destinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val EVENT_ENTRY = "event_entry"
    const val EVENT_ENTRY_EDIT = "event_entry/{eventId}/{eventTitle}/{eventDate}/{eventDescription}"
}

@Composable
fun AuthApp() {
    val navController = rememberNavController()
    AuthNavGraph(navController)
}

@Composable
fun AuthNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.LOGIN
    ) {
        composable(Destinations.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Destinations.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(Destinations.HOME) {
                        // Borra la pila hasta LOGIN (inclusive) para que el usuario no pueda volver al login con el botón 'atrás'
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Destinations.HOME) {
                        // Borra la pila hasta LOGIN (inclusive)
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Destinations.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        // Borra la pila hasta HOME (inclusive) para volver a la pantalla de LOGIN
                        popUpTo(Destinations.HOME) { inclusive = true }
                    }
                },
                onNavigateToAddEvent = {
                    navController.navigate(Destinations.EVENT_ENTRY)
                },
                onNavigateToEditEvent = { eventId, eventTitle, eventDate, eventDescription ->
                    val encodedTitle = URLEncoder.encode(eventTitle, StandardCharsets.UTF_8.toString())
                    val encodedDate = URLEncoder.encode(eventDate, StandardCharsets.UTF_8.toString())
                    val encodedDescription = URLEncoder.encode(eventDescription, StandardCharsets.UTF_8.toString())
                    navController.navigate("event_entry/$eventId/$encodedTitle/$encodedDate/$encodedDescription")
                }
            )
        }

        // Pantalla para agregar evento
        composable(Destinations.EVENT_ENTRY) {
            EventEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla para editar evento
        composable(
            route = Destinations.EVENT_ENTRY_EDIT,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("eventTitle") { type = NavType.StringType },
                navArgument("eventDate") { type = NavType.StringType },
                navArgument("eventDescription") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val eventTitle = backStackEntry.arguments?.getString("eventTitle")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            }
            val eventDate = backStackEntry.arguments?.getString("eventDate")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            }
            val eventDescription = backStackEntry.arguments?.getString("eventDescription")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            }

            EventEntryScreen(
                eventId = eventId,
                eventTitle = eventTitle,
                eventDate = eventDate,
                eventDescription = eventDescription,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}