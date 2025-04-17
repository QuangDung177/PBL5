package com.example.pbl5.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pbl5.ui.screens.FishScreen
import com.example.pbl5.ui.screens.MainScreen
import com.example.pbl5.ui.screens.MonitorScreen
import com.example.pbl5.ui.screens.SettingsScreen
import com.example.pbl5.ui.viewmodel.FishViewModel
import com.example.pbl5.ui.viewmodel.MainViewModel
import com.example.pbl5.ui.viewmodel.MonitorViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current // Lấy context từ Compose

    NavHost(
        navController = navController,
        startDestination = "Home",
        modifier = modifier
    ) {
        composable("Home") {
            MainScreen(viewModel = viewModel, navController = navController)
        }
        composable("Fish") {
            val fishViewModel = FishViewModel(context, viewModel) // Truyền context vào FishViewModel
            FishScreen(navController = navController, mainViewModel = viewModel, fishViewModel = fishViewModel)
        }
        composable("Monitor") {
            val monitorViewModel = MonitorViewModel(viewModel, context) // Truyền context vào MonitorViewModel
            MonitorScreen(navController = navController, mainViewModel = viewModel, monitorViewModel = monitorViewModel)
        }
        composable("Settings") {
            SettingsScreen(navController = navController, mainViewModel = viewModel)
        }
    }
}