package com.mnn.cameranote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mnn.cameranote.navigation.Destinations.EDIT_TITLE_ROUTE
import com.mnn.cameranote.navigation.Destinations.createDetailRoute
import com.mnn.cameranote.navigation.Destinations.createEditTitleRoute
import com.mnn.cameranote.navigation.Destinations.createInfoRoute
import com.mnn.cameranote.screens.camera.CameraScreen
import com.mnn.cameranote.screens.messagedetail.MessageDetailScreen
import com.mnn.cameranote.screens.messagedetail.messageinfo.GenericEditScreen
import com.mnn.cameranote.screens.messagedetail.messageinfo.MessageInfoScreen
import com.mnn.cameranote.screens.messagelist.MessageScreen

@Composable
fun AppNavHost(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, startDestination = Destinations.GALLERY_ROUTE, modifier = modifier
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
            )) {
            MessageDetailScreen(
                onBack = { navController.popBackStack() },
                onEditClick = { navController.navigate(createInfoRoute(it)) })
        }
        composable(
            Destinations.MESSAGE_INFO_ROUTE,
            arguments = listOf(navArgument("id") { type = NavType.LongType } // 声明参数名和类型
            )) {
            MessageInfoScreen(
                onBack = { navController.popBackStack() },
                onUpdateTitle = {
                    navController.navigate(createEditTitleRoute(it))
                },
                onUpdateDetail = { },
                onDelete = {
                    navController.navigate(Destinations.GALLERY_ROUTE) {
                        popUpTo(Destinations.GALLERY_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        composable(
            EDIT_TITLE_ROUTE, arguments = listOf(
                navArgument("id") { type = NavType.LongType },
            )) {
            GenericEditScreen(
                label = "修改标题",
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
    }
}

