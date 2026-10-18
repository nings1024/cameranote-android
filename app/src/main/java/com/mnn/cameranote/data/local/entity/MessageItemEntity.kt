package com.mnn.cameranote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/**
 * 备忘里的一条内容（一张照片 / 一段文字）。
 *
 * 排序统一使用 [itemId]（自增，天然等于插入顺序），[sequence] 仅作为将来
 * 支持手动排序的预留字段。
 *
 * 对 [MessageEntity] 建了外键：[onDelete] 用 CASCADE 而不是 NO_ACTION，
 * 是为了在**将来**真的需要物理删除父行时（例如清理草稿、导入导出重建）不会
 * 留下无主的孤儿条目。当前业务走软删除，不会触发级联，但约束本身能保证
 * 「条目一定属于一个存在的备忘」这个不变量。
 *
 * 注意：SQLite 的外键默认不生效，实际校验依赖
 * `AppDatabase.FOREIGN_KEY_CALLBACK` 在打开数据库时启用 `PRAGMA foreign_keys`。
 */
@Entity(
    tableName = "message_items",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["messageId", "type", "sequence"]),
        Index(value = ["isDeleted", "createTime"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MessageItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val messageId: Long,
    val sequence: Int,
    val type: MessageItemType,
    /** 图片为本地绝对路径，文字为文本内容 */
    val content: String,
    val source: MessageItemSource = MessageItemSource.CAMERA,
    val isDeleted: Boolean = false,
    val deletedTime: Long = 0,
    val createTime: Long = 0,
)

enum class MessageItemType(val value: Int) {
    TEXT(0), IMAGE(1), VIDEO(2);

    companion object {
        fun fromValue(value: Int): MessageItemType {
            return entries.find { it.value == value } ?: TEXT
        }
    }
}

enum class MessageItemSource(val value: Int) {
    CAMERA(0), UPLOAD(1);

    companion object {
        fun fromValue(value: Int): MessageItemSource {
            return entries.find { it.value == value } ?: CAMERA
        }
    }
}

/** 枚举 <-> Int 的 Room 类型转换器。 */
class MessageItemConverters {
    @TypeConverter
    fun fromMessageItemType(type: MessageItemType): Int = type.value

    @TypeConverter
    fun toMessageItemType(value: Int): MessageItemType = MessageItemType.fromValue(value)

    @TypeConverter
    fun fromMessageItemSource(source: MessageItemSource): Int = source.value

    @TypeConverter
    fun toMessageItemSource(value: Int): MessageItemSource = MessageItemSource.fromValue(value)
}
