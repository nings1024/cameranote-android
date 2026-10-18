package com.mnn.cameranote.ui.image

import android.content.Context
import coil3.request.ImageRequest
import java.io.File

/**
 * 统一的本地图片请求构建。
 *
 * 两个作用：
 * 1. 所有地方都用 `File` 作为 model，不依赖 Coil 对裸路径字符串的隐式推断；
 * 2. 通过 `size` 限制解码尺寸，避免列表缩略图把 12MP 原图整张解进内存。
 */
fun imageRequest(
    context: Context,
    path: String,
    sizePx: Int? = null,
): ImageRequest {
    val builder = ImageRequest.Builder(context).data(File(path))
    if (sizePx != null && sizePx > 0) {
        builder.size(sizePx)
    }
    return builder.build()
}
