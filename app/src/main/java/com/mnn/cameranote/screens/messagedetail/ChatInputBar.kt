package com.mnn.cameranote.screens.messagedetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendText: () -> Unit,
    onAddImage: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.imePadding() // 关键：自动避让软键盘
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAddImage) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "选择图片", tint = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSendText,
                enabled = text.isNotBlank(),
                shape = CircleShape
            ) {
                Text("发送")
            }
        }
    }
}