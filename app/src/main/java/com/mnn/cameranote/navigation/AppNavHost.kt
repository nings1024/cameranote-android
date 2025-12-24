package com.mnn.cameranote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mnn.cameranote.navigation.Destinations.createDetailRoute
import com.mnn.cameranote.screens.camera.CameraScreen
import com.mnn.cameranote.screens.messagedetail.MessageDetailScreen
import com.mnn.cameranote.screens.messagelist.MessageScreen

@Composable
fun AppNavHost(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, startDestination = Destinations.CAMERA_ROUTE, modifier = modifier
    ) {
        composable(Destinations.CAMERA_ROUTE) {
            CameraScreen(onNavigateToGallery = {
                navController.navigate(Destinations.GALLERY_ROUTE)
            })
        }
        composable(Destinations.GALLERY_ROUTE) {
            MessageScreen(onBack = { navController.popBackStack() }, onDetailClick = {
                navController.navigate(createDetailRoute(it))
            })
        }
        composable(
            Destinations.DETAIL_ROUTE,
            arguments = listOf(navArgument("id") { type = NavType.LongType } // 声明参数名和类型
            )) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getLong("id") ?: 1
            MessageDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}