package com.heruteguhapriyant.tasky.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heruteguhapriyant.tasky.ui.dashboard.DashboardScreen
import com.heruteguhapriyant.tasky.ui.more.MoreScreen
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
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToCreateTask = {
                        navController.navigate(Screen.CreateTask.route)
                    }
                )
            }

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
        }
    }
}
