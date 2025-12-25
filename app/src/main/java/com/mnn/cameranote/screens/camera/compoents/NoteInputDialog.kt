package com.mnn.cameranote.screens.camera.compoents

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun NoteInputDialog(
    isVisible: Boolean,
    noteText: String,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val focusRequester = remember { FocusRequester() }

    // 只要显示出来，就自动请求焦点弹起键盘
    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() } // 点击遮罩层关闭
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    color = Color(0xFF1C1C1E).copy(alpha = 0.95f), // 深色磨砂感
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(20.dp)
        ) {
            Text(
                text = "发生什么了",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = noteText,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("想要记录点什么...", color = Color.DarkGray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,    // 聚焦时背景
                    unfocusedContainerColor = Color.Transparent,  // 未聚焦时背景
                    disabledContainerColor = Color.Transparent,   // 禁用时背景
                    focusedTextColor = Color.White,               // 聚焦时文字颜色
                    unfocusedTextColor = Color.White,             // 未聚焦时文字颜色
                    cursorColor = Color.Cyan,                     // 光标颜色
                    focusedIndicatorColor = Color.Cyan,           // 聚焦时底部指示线条
                    unfocusedIndicatorColor = Color.Gray          // 未聚焦时底部指示线条
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDismiss() })
            )
        }
    }
}