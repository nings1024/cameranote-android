package com.mnn.cameranote.screens.test

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun TestScreen(onBack: () -> Unit) {
    val balls = remember { mutableStateListOf<Ball>() } // 可变的球体列表
    val scope = rememberCoroutineScope() // 用于启动协程

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // 游戏背景
            .pointerInput(Unit) { // 核心：获取点击位置
                detectTapGestures(
                    onTap = { offset ->
                        // 每次点击，生成一个新球
                        val newBall = Ball(
                            color = Color(
                                red = (0..255).random(),
                                green = (0..255).random(),
                                blue = (0..255).random(),
                                alpha = 255
                            ),
                            initialOffset = offset,
                            targetY = size.height.toFloat() + 50f // 掉落到屏幕外一点点
                        )
                        balls.add(newBall) // 添加到列表
                    }
                )
            }
    ) {
        // 遍历所有球体，并渲染它们
        balls.forEach { ball ->
            // 每个球都是一个独立的 Composable
            FallingBall(
                ball = ball,
                onAnimationEnd = { endedBall ->
                    // 动画结束后，将球从列表中移除
                    balls.remove(endedBall)
                }
            )
        }
    }
}

// Ball 类的定义，和上面一样
data class Ball(
    val id: String = UUID.randomUUID().toString(),
    val color: Color,
    val initialOffset: Offset,
    val targetY: Float // 目标掉落Y坐标
)

@Composable
fun FallingBall(ball: Ball, onAnimationEnd: (Ball) -> Unit) {
    // Animatable 用于控制球的Y坐标从 initialOffset.y 动画到 ball.targetY
    val animatedY = remember { Animatable(ball.initialOffset.y) }
    val scope = rememberCoroutineScope()

    // 当 Composable 进入组合树时，启动掉落动画
    LaunchedEffect(ball.id) {
        // 启动动画
        animatedY.animateTo(
            targetValue = ball.targetY,
            animationSpec = tween(durationMillis = 1500) // 1.5秒掉落
        )
        // 动画结束后触发回调，通知父组件移除此球
        onAnimationEnd(ball)
    }

    // 绘制球体
    Canvas(
        modifier = Modifier
            // 关键：将 Canvas 的位置设置为初始X和动画化的Y
            .offset(x = ball.initialOffset.x.dp, y = animatedY.value.dp)
    ) {
        drawCircle(
            color = ball.color,
            radius = 30.dp.toPx() // 球的半径
        )
    }
}