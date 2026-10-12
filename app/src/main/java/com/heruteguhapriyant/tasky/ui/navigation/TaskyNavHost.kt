package com.heruteguhapriyant.tasky.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.heruteguhapriyant.tasky.ui.category.CategoryManagementScreen
import com.heruteguhapriyant.tasky.ui.dashboard.DashboardScreen
import com.heruteguhapriyant.tasky.ui.more.MoreScreen
import com.heruteguhapriyant.tasky.ui.onboarding.OnboardingScreen
import com.heruteguhapriyant.tasky.ui.settings.SettingsScreen
import com.heruteguhapriyant.tasky.ui.splash.SplashScreen
import com.heruteguhapriyant.tasky.ui.taskdetail.TaskDetailScreen
import com.heruteguhapriyant.tasky.ui.taskform.TaskFormScreen
import com.heruteguhapriyant.tasky.ui.tasklist.TaskListScreen

@Composable
fun TaskyApp(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            TaskyBottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding Screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Dashboard Screen
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToTaskDetail = { taskId ->
                        navController.navigate(Screen.TaskDetail.createRoute(taskId))
                    }
                )
            }

            // Task List Screen
            composable(Screen.TaskList.route) {
                TaskListScreen(
                    onNavigateToCreateTask = {
                        navController.navigate(Screen.CreateTask.route)
                    },
                    onNavigateToTaskDetail = { taskId ->
                        navController.navigate(Screen.TaskDetail.createRoute(taskId))
                    }
                )
            }

            // More Screen
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToCategories = {
                        navController.navigate(Screen.CategoryManagement.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Create Task Screen
            composable(Screen.CreateTask.route) {
                TaskFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Edit Task Screen
            composable(
                route = Screen.EditTask.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) {
                TaskFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Task Detail Screen
            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) {
                TaskDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditTask = { taskId ->
                        navController.navigate(Screen.EditTask.createRoute(taskId))
                    }
                )
            }

            // Category Management Screen
            composable(Screen.CategoryManagement.route) {
                CategoryManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
