package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivy.data.db.entity.ParsedNotificationEntity
import kotlinx.coroutines.flow.Flow

import androidx.room.Upsert

@Dao
interface ParsedNotificationDao {
    @Query("SELECT * FROM parsed_notifications WHERE isUsed = 0 AND timestamp >= (strftime('%s', 'now') * 1000 - 432000000) ORDER BY timestamp DESC")
    fun getPendingNotificationsFlow(): Flow<List<ParsedNotificationEntity>>

    @Query("SELECT * FROM parsed_notifications WHERE isUsed = 0 AND timestamp >= :cutoffTimestamp ORDER BY timestamp DESC")
    fun getPendingNotificationsFlow(cutoffTimestamp: Long): Flow<List<ParsedNotificationEntity>>

    @Query("SELECT * FROM parsed_notifications WHERE isUsed = 0 AND timestamp >= (strftime('%s', 'now') * 1000 - 432000000) ORDER BY timestamp DESC")
    suspend fun getPendingNotifications(): List<ParsedNotificationEntity>

    @Query("SELECT * FROM parsed_notifications WHERE isUsed = 0 AND timestamp >= :cutoffTimestamp ORDER BY timestamp DESC")
    suspend fun getPendingNotifications(cutoffTimestamp: Long): List<ParsedNotificationEntity>

    @Query("SELECT COUNT(*) FROM parsed_notifications WHERE packageName = :packageName AND amount = :amount AND isUsed = 0 AND timestamp >= :recentCutoff")
    suspend fun countRecentDuplicates(packageName: String, amount: Double, recentCutoff: Long): Int

    @Upsert
    suspend fun upsert(notification: ParsedNotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: ParsedNotificationEntity)

    @Query("UPDATE parsed_notifications SET isUsed = 1 WHERE id = :id")
    suspend fun markAsUsed(id: String)

    @Query("UPDATE parsed_notifications SET isUsed = 1")
    suspend fun markAllAsUsed()

    @Query("DELETE FROM parsed_notifications WHERE isUsed = 1 OR timestamp < (strftime('%s', 'now') * 1000 - 432000000)")
    suspend fun deleteOldOrUsed()

    @Query("DELETE FROM parsed_notifications WHERE isUsed = 1 OR timestamp < :cutoffTimestamp")
    suspend fun deleteOldOrUsed(cutoffTimestamp: Long)
}
