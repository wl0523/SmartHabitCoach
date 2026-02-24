package com.example.smarthabitcoach.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smarthabitcoach.habits.HabitViewModel
import com.example.smarthabitcoach.habits.ui.HabitScreen

/**
 * NavHost composable for the entire app.
 * Configures all navigation routes and screen destinations.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HABITS,
        modifier = modifier
    ) {
        composable(Routes.HABITS) {
            val viewModel: HabitViewModel = hiltViewModel()
            HabitScreen(viewModel)
        }
    }
}

