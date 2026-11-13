package com.hucw.xinqing.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hucw.xinqing.ui.screens.CourseEditScreen
import com.hucw.xinqing.ui.screens.HomeScreen
import com.hucw.xinqing.ui.screens.PlaceholderScreen
import com.hucw.xinqing.ui.screens.SettingsScreen
import com.hucw.xinqing.ui.screens.TimetableScreen

private data class Tab(val route: String, val label: String, val symbol: String)

private val tabs = listOf(
    Tab("today", "今天", "今"),
    Tab("diary", "日记", "记"),
    Tab("timetable", "课表", "课"),
    Tab("review", "回顾", "顾"),
    Tab("settings", "设置", "设")
)

@Composable
fun XinqingNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = { navController.navigateTab(tab.route) },
                        icon = { Text(tab.symbol) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("today") { HomeScreen() }
            composable("diary") {
                PlaceholderScreen("我的日记", "日记与周记将在后续阶段加入")
            }
            composable("timetable") {
                TimetableScreen(
                    onAdd = { navController.navigate("course/0") },
                    onEdit = { id -> navController.navigate("course/$id") },
                    onOpenSettings = { navController.navigateTab("settings") }
                )
            }
            composable(
                "course/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                CourseEditScreen(
                    courseId = id,
                    onDone = { navController.popBackStack() }
                )
            }
            composable("review") {
                PlaceholderScreen("生活回顾", "心情汇总与周回顾将在后续阶段加入")
            }
            composable("settings") { SettingsScreen() }
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        launchSingleTop = true
        popUpTo(graph.startDestinationId) { saveState = true }
        restoreState = true
    }
}
