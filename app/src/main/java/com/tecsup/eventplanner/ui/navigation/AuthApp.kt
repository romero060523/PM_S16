package com.tecsup.eventplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.eventplanner.ui.screens.LoginScreen
import com.tecsup.eventplanner.ui.screens.RegisterScreen

object Destinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val COURSE_ENTRY = "course_entry"
    const val COURSE_ENTRY_EDIT = "course_entry/{courseId}/{courseName}/{courseDescription}"
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
                onNavigateToAddCourse = {
                    navController.navigate(Destinations.COURSE_ENTRY)
                },
                onNavigateToEditCourse = { courseId, courseName, courseDescription ->
                    navController.navigate("course_entry/$courseId/$courseName/$courseDescription")
                }
            )
        }

        // Pantalla para agregar curso
        composable(Destinations.COURSE_ENTRY) {
            CourseEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla para editar curso
        composable(
            route = Destinations.COURSE_ENTRY_EDIT,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("courseName") { type = NavType.StringType },
                navArgument("courseDescription") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            val courseName = backStackEntry.arguments?.getString("courseName")
            val courseDescription = backStackEntry.arguments?.getString("courseDescription")

            CourseEntryScreen(
                courseId = courseId,
                courseName = courseName,
                courseDescription = courseDescription,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}