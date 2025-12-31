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
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Camera, // 直接传入对象
        modifier = modifier
    ) {
        // 1. 相机页
        composable<Route.Camera> {
            CameraScreen(onNavigateToGallery = {
                navController.navigate(Route.Gallery)
            })
        }

        // 2. 列表页
        composable<Route.Gallery> {
            MessageScreen(
                onBack = { navController.popBackStack() },
                onDetailClick = { id ->
                    navController.navigate(Route.Detail(id)) // 自动序列化参数
                }
            )
        }

        // 3. 详情页
        composable<Route.Detail> { backStackEntry ->
            // 自动从路由中解析出参数，无需定义 Key
            MessageDetailScreen(
                onBack = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Route.MessageInfo(id))
                }
            )
        }

        // 4. 信息管理页
        composable<Route.MessageInfo> { backStackEntry ->
            MessageInfoScreen(
                onBack = { navController.popBackStack() },
                onUpdateTitle = { id -> navController.navigate(Route.EditTitle(id)) },
                onUpdateDetail = { id -> navController.navigate(Route.EditDetail(id)) },
                onDelete = {
                    navController.navigate(Route.Gallery) {
                        popUpTo<Route.Gallery> { inclusive = true }
                    }
                }
            )
        }

        // 5. 通用编辑页 (标题/描述)
        composable<Route.EditTitle> {
            GenericEditScreen(
                label = "修改标题",
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }

        composable<Route.EditDetail> {
            GenericDetailEditScreen(
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
    }
}

