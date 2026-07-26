package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration131to132_AlternativeAmounts : Migration(131, 132) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `parsed_notifications` ADD COLUMN `alternativeAmounts` TEXT DEFAULT NULL")
    }
}
