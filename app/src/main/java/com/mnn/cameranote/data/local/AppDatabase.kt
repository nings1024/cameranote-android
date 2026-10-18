package com.mnn.cameranote.data.local

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mnn.cameranote.data.local.dao.MessageDao
import com.mnn.cameranote.data.local.entity.MessageEntity
import com.mnn.cameranote.data.local.entity.MessageItemConverters
import com.mnn.cameranote.data.local.entity.MessageItemEntity

@Database(
    entities = [MessageEntity::class, MessageItemEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(MessageItemConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {

        const val NAME = "camera_note_db"

        /**
         * v1 -> v2：
         * 1. 修正 `messages.deletedTIme` 拼写 -> `deletedTime`（API 26 的 SQLite 3.18 不支持
         *    RENAME COLUMN，所以用「建新表 + 拷贝 + 改名」的标准做法）；
         * 2. 补全查询索引，列表页的相关子查询不再全表扫描；
         * 3. 清理历史孤儿数据（主表已软删、子项仍标记为有效）。
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `messages_new`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `messages_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `location` TEXT NOT NULL,
                        `detailInfo` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `deletedTime` INTEGER NOT NULL,
                        `year` INTEGER NOT NULL,
                        `month` INTEGER NOT NULL,
                        `day` INTEGER NOT NULL,
                        `isTemp` INTEGER NOT NULL,
                        `createTime` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `messages_new`
                        (`id`, `title`, `location`, `detailInfo`, `isDeleted`, `deletedTime`,
                         `year`, `month`, `day`, `isTemp`, `createTime`)
                    SELECT
                        `id`, `title`, `location`, `detailInfo`, `isDeleted`, `deletedTIme`,
                        `year`, `month`, `day`, `isTemp`, `createTime`
                    FROM `messages`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `messages`")
                db.execSQL("ALTER TABLE `messages_new` RENAME TO `messages`")

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_messages_isDeleted_createTime` " +
                        "ON `messages` (`isDeleted`, `createTime`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_messages_isTemp_createTime` " +
                        "ON `messages` (`isTemp`, `createTime`)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_message_items_messageId` " +
                        "ON `message_items` (`messageId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_message_items_messageId_type_sequence` " +
                        "ON `message_items` (`messageId`, `type`, `sequence`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_message_items_isDeleted_createTime` " +
                        "ON `message_items` (`isDeleted`, `createTime`)"
                )

                db.execSQL(
                    """
                    UPDATE `message_items`
                    SET `isDeleted` = 1, `deletedTime` = CAST(strftime('%s', 'now') AS INTEGER) * 1000
                    WHERE `isDeleted` = 0
                      AND `messageId` IN (SELECT `id` FROM `messages` WHERE `isDeleted` = 1)
                    """.trimIndent()
                )
            }
        }

        /**
         * v2 -> v3：给 `message_items.messageId` 加上指向 `messages.id` 的外键
         * （ON DELETE CASCADE）。
         *
         * SQLite 不支持 `ALTER TABLE ... ADD CONSTRAINT`，只能重建子表。
         * 重建前必须先删掉「指向不存在的备忘」的历史孤儿条目，否则建立约束会失败——
         * 这些行在 UI 上本来也永远不会被读到（列表和时间线都靠 messageId 关联）。
         *
         * 只重建子表，不动父表，因此不会触发任何级联删除。
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "DELETE FROM `message_items` " +
                        "WHERE `messageId` NOT IN (SELECT `id` FROM `messages`)"
                )

                db.execSQL("DROP TABLE IF EXISTS `message_items_new`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `message_items_new` (
                        `itemId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `sequence` INTEGER NOT NULL,
                        `type` INTEGER NOT NULL,
                        `content` TEXT NOT NULL,
                        `source` INTEGER NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `deletedTime` INTEGER NOT NULL,
                        `createTime` INTEGER NOT NULL,
                        FOREIGN KEY(`messageId`) REFERENCES `messages`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `message_items_new`
                        (`itemId`, `messageId`, `sequence`, `type`, `content`, `source`,
                         `isDeleted`, `deletedTime`, `createTime`)
                    SELECT
                        `itemId`, `messageId`, `sequence`, `type`, `content`, `source`,
                        `isDeleted`, `deletedTime`, `createTime`
                    FROM `message_items`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `message_items`")
                db.execSQL("ALTER TABLE `message_items_new` RENAME TO `message_items`")

                // DROP TABLE 会一并删掉旧表上的索引，必须重建
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_message_items_messageId` " +
                        "ON `message_items` (`messageId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_message_items_messageId_type_sequence` " +
                        "ON `message_items` (`messageId`, `type`, `sequence`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_message_items_isDeleted_createTime` " +
                        "ON `message_items` (`isDeleted`, `createTime`)"
                )
            }
        }

        /** 升级链，按版本顺序注册。 */
        val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

        /**
         * SQLite 的 `PRAGMA foreign_keys` 默认是关闭的，Room 自己也不会打开
         * （`RoomOpenHelper.onConfigure` 只是透传给父类）。
         *
         * 不打开的话 `@ForeignKey` 只是一段 schema 声明，插入时根本不会校验，
         * 所以这里在每次数据库打开后显式启用连接池级别的外键约束。
         * 用 runCatching 兜住：万一某个 ROM 上不允许这样配置，也只是退化成
         * 「约束不强制」，绝不能让数据库打不开。
         */
        val FOREIGN_KEY_CALLBACK = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                runCatching { db.setForeignKeyConstraintsEnabled(true) }
                    .onFailure { Log.w(TAG, "启用外键约束失败，将退化为不强制", it) }
            }
        }

        private const val TAG = "AppDatabase"
    }
}
