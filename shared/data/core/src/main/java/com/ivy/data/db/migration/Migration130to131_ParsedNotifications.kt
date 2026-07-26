package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration130to131_ParsedNotifications : Migration(130, 131) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `parsed_notifications` (
                `id` TEXT NOT NULL,
                `packageName` TEXT NOT NULL,
                `title` TEXT,
                `text` TEXT,
                `amount` REAL NOT NULL,
                `currency` TEXT,
                `timestamp` INTEGER NOT NULL,
                `isUsed` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}
