package com.mnn.cameranote.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 一条「备忘」。
 *
 * 设计理念：拍照优先、描述后补。连续拍摄的多张照片会先落进同一个草稿
 * （[isTemp] = true），等用户补上描述后 [isTemp] 置为 false 表示已归档。
 */
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["isDeleted", "createTime"]),
        Index(value = ["isTemp", "createTime"]),
    ],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 主题。草稿期是日期，补完描述后是用户写的标题。 */
    val title: String,
    /** 地点（暂未采集，保留字段） */
    val location: String,
    /** 详细说明 */
    val detailInfo: String,
    /** 软删除标记 */
    val isDeleted: Boolean = false,
    /** 删除时间戳，0 表示未删除 */
    val deletedTime: Long = 0,
    val year: Int = 1,
    val month: Int = 1,
    val day: Int = 1,
    /** true = 还在收集中的草稿 */
    val isTemp: Boolean = false,
    /** 创建时间（毫秒时间戳），排序依据 */
    val createTime: Long = 0,
)
