package com.mnn.cameranote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mnn.cameranote.screens.camera.CameraScreen
import com.mnn.cameranote.screens.messagedetail.MessageDetailScreen
import com.mnn.cameranote.screens.messagedetail.messageinfo.GenericDetailEditScreen
import com.mnn.cameranote.screens.messagedetail.messageinfo.GenericEditScreen
import com.mnn.cameranote.screens.messagedetail.messageinfo.MessageInfoScreen
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
                navController.navigate(Destinations.createDetailRoute(it))
            })
        }
        composable(
            Destinations.DETAIL_ROUTE,
            arguments = listOf(navArgument("id") { type = NavType.LongType } // 声明参数名和类型
            )) {
            MessageDetailScreen(
                onBack = { navController.popBackStack() },
                onEditClick = { navController.navigate(Destinations.createInfoRoute(it)) })
        }
        composable(
            Destinations.MESSAGE_INFO_ROUTE,
            arguments = listOf(navArgument("id") { type = NavType.LongType } // 声明参数名和类型
            )) {
            MessageInfoScreen(
                onBack = { navController.popBackStack() },
                onUpdateTitle = {
                    navController.navigate(Destinations.createEditTitleRoute(it))
                },
                onUpdateDetail = {
                    navController.navigate(Destinations.createEditDetailRoute(it))
                },
                onDelete = {
                    navController.navigate(Destinations.GALLERY_ROUTE) {
                        popUpTo(Destinations.GALLERY_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        composable(
            Destinations.EDIT_TITLE_ROUTE, arguments = listOf(
                navArgument("id") { type = NavType.LongType },
            )
        ) {
            GenericEditScreen(
                label = "修改标题",
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        composable(
            Destinations.EDIT_DETAIL_ROUTE, arguments = listOf(
                navArgument("id") { type = NavType.LongType },
            )
        ) {
            GenericDetailEditScreen(
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
    }
}

