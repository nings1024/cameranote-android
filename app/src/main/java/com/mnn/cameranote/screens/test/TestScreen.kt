package com.mnn.cameranote.screens.test

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun TestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // 记录当前目的地的索引
    var currentDestinationIndex by remember { mutableIntStateOf(0) }

    // 定义目的地列表
    val destinations = listOf("screen1", "screen2", "screen3")

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                // 循环切换到下一个目的地
                currentDestinationIndex = (currentDestinationIndex + 1) % destinations.size
                val destination = destinations[currentDestinationIndex]
                navController.navigate(destination)
                Toast.makeText(context, "切换到页面${currentDestinationIndex + 1}", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("切换页面")
        }

        Spacer(modifier = Modifier.height(50.dp))

        // NavHost
        NavHost(
            navController = navController,
            startDestination = destinations[0],
            modifier = Modifier.fillMaxSize()
        ) {
            composable(destinations[0]) {
                Screen1()
            }
            composable(destinations[1]) {
                Screen2(navController)
            }
            composable(destinations[2]) {
                Screen3()
            }
        }
    }
}

@Composable
fun Screen1() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("页面1 - 字符串1", fontSize = 30.sp)
    }
}

@Composable
fun Screen2(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("页面2 - 字符串2", fontSize = 30.sp)
        Button(onClick = { navController.navigate("screen3") }) {
            Text("按钮")
        }
    }
}

@Composable
fun Screen3() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("页面3 - 字符串3", fontSize = 30.sp)
    }
}