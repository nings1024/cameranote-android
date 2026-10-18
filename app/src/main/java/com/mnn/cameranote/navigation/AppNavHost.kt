package com.mnn.cameranote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mnn.cameranote.screens.camera.CameraScreen
import com.mnn.cameranote.screens.messagedetail.MessageDetailScreen
import com.mnn.cameranote.screens.messagedetail.messageinfo.GenericDetailEditScreen
import com.mnn.cameranote.screens.messagedetail.messageinfo.GenericEditScreen
import com.mnn.cameranote.screens.messagedetail.messageinfo.MessageInfoScreen
import com.mnn.cameranote.screens.messagelist.MessageScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Camera,
        modifier = modifier,
    ) {
        // 1. 相机页（首页）
        composable<Route.Camera> {
            CameraScreen(
                onNavigateToGallery = { navController.navigate(Route.Gallery) },
            )
        }

        // 2. 备忘列表
        composable<Route.Gallery> {
            MessageScreen(
                onBack = { navController.popBackStack() },
                onDetailClick = { id -> navController.navigate(Route.Detail(id)) },
            )
        }

        // 3. 备忘详情
        composable<Route.Detail> {
            MessageDetailScreen(
                onBack = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(Route.MessageInfo(id)) },
            )
        }

        // 4. 备忘设置
        composable<Route.MessageInfo> {
            MessageInfoScreen(
                onBack = { navController.popBackStack() },
                onUpdateTitle = { id -> navController.navigate(Route.EditTitle(id)) },
                onUpdateDetail = { id -> navController.navigate(Route.EditDetail(id)) },
                onDelete = {
                    navController.navigate(Route.Gallery) {
                        popUpTo<Route.Gallery> { inclusive = true }
                    }
                },
            )
        }

        // 5. 通用编辑页（标题 / 描述）
        composable<Route.EditTitle> {
            GenericEditScreen(
                label = "修改主题",
                hint = "给这条备忘起个名字",
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() },
            )
        }

        composable<Route.EditDetail> {
            GenericDetailEditScreen(
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() },
            )
        }
    }
}
