package com.example.pbl5.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pbl5.ui.screens.FishScreen
import com.example.pbl5.ui.screens.MainScreen
import com.example.pbl5.ui.screens.MonitorScreen
import com.example.pbl5.ui.screens.SettingsScreen
import com.example.pbl5.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "Home",
        modifier = modifier
    ) {
        composable("Home") {
            MainScreen(viewModel = viewModel, navController = navController)
        }
        composable("Fish") {
            FishScreen(navController = navController, mainViewModel = viewModel)
        }
        composable("Monitor") {
            MonitorScreen(navController = navController, mainViewModel = viewModel)
        }
        composable("Settings") {
            SettingsScreen(navController = navController, mainViewModel = viewModel)
        }
    }
}