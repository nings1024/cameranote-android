package com.mnn.cameranote.screens.messagelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NoteList(messages: List<MessageUiModel>,paddingValues:PaddingValues,onItemClick: (Long) -> Unit) {
    LazyColumn(contentPadding = paddingValues) {
        items(messages) { message ->
            Column {
                ChatItem(message,onClick=onItemClick)
                // 仿微信分割线：起始位置避开头像
                HorizontalDivider(
                    modifier = Modifier.padding(start = 76.dp),
                    thickness = 0.5.dp,
                    color = Color(0xFFEEEEEE)
                )
            }
        }
    }
}

@Composable
fun ChatItem(message: MessageUiModel, onClick: (Long) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick(message.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1. 图标/头像 (带圆角)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.LightGray) // 占位图背景
        ) {
            val imageFile = File(message.content)
            AsyncImage(
                model = imageFile, // Coil 支持直接传入 File 对象
                contentDescription = "头像",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop, // 类似 CenterCrop，填满并裁剪
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 中间和右边的内容容器
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 2. 主题/名称
                Text(
                    text = message.title,
                    fontSize = 17.sp,
                    color = Color(0xFF111111),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 4. 右侧时间
                Text(
                    text = message.createTime.toDateTimeString(),
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. 详情/消息内容
            Text(
                text = message.detailInfo,
                fontSize = 14.sp,
                color = Color(0xFF999999),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun Long.toDateTimeString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault()) // 使用系统当前时区
    return formatter.format(Instant.ofEpochMilli(this))
}