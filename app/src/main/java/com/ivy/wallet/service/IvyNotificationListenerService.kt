package com.ivy.wallet.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ivy.base.legacy.SharedPrefs
import com.ivy.data.db.IvyRoomDatabase
import com.ivy.data.db.entity.ParsedNotificationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject

import java.util.concurrent.ConcurrentHashMap

@AndroidEntryPoint
class IvyNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var sharedPrefs: SharedPrefs

    @Inject
    lateinit var database: IvyRoomDatabase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        private val patternCache = ConcurrentHashMap<String, Pattern>()

        fun getOrCompilePattern(patternStr: String): Pattern {
            return patternCache.computeIfAbsent(patternStr) { Pattern.compile(it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        serviceScope.launch {
            processNotification(sbn)
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification) {
        val isEnabled = sharedPrefs.getBoolean(SharedPrefs.NOTIFICATION_PARSER_ENABLED, false)
        if (!isEnabled) return

        val targetPackageString = sharedPrefs.getString(SharedPrefs.NOTIFICATION_TARGET_PACKAGE, "")?.trim() ?: ""
        val targetPackages = targetPackageString.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (targetPackages.isNotEmpty()) {
            val matches = targetPackages.any { it.equals(sbn.packageName, ignoreCase = true) }
            if (!matches) return
        }

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val fullText = listOfNotNull(title, text, subText).joinToString(" ")
        if (fullText.isBlank()) return

        val defaultPattern = "\\(?(-?\\d+(?:[\\.,]\\d+)?)\\s*([A-Za-z]{3})\\)?"
        val customPattern = sharedPrefs.getString(
            SharedPrefs.NOTIFICATION_REGEX_PATTERN,
            defaultPattern
        )
        val regexPatternString = if (customPattern.isNullOrBlank()) defaultPattern else customPattern

        try {
            val pattern = getOrCompilePattern(regexPatternString)
            val extracted = extractNotificationAmounts(title, fullText, pattern) ?: return
            val amount = extracted.mainMatch.amount
            val currency = extracted.mainMatch.currency
            val altString = if (extracted.alternativeAmounts.isNotEmpty()) {
                extracted.alternativeAmounts.joinToString(",")
            } else null

            val notificationId = if (!sbn.key.isNullOrBlank()) {
                sbn.key
            } else {
                "${sbn.packageName}_${sbn.id}_${sbn.postTime}"
            }

            val entity = ParsedNotificationEntity(
                id = notificationId,
                packageName = sbn.packageName,
                title = title ?: sbn.packageName,
                text = text ?: fullText,
                amount = amount,
                currency = currency,
                timestamp = System.currentTimeMillis(),
                isUsed = false,
                alternativeAmounts = altString
            )

            withContext(Dispatchers.IO) {
                database.parsedNotificationDao.deleteOldOrUsed()
                val recentCutoff = System.currentTimeMillis() - 5000L
                val duplicates = database.parsedNotificationDao.countRecentDuplicates(
                    packageName = sbn.packageName,
                    amount = amount,
                    recentCutoff = recentCutoff
                )
                if (duplicates == 0) {
                    database.parsedNotificationDao.upsert(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private data class MatchResultData(
        val amount: Double,
        val currency: String?,
        val isTitleMatch: Boolean,
        val isSigned: Boolean
    )

    private data class ExtractedNotificationAmounts(
        val mainMatch: MatchResultData,
        val alternativeAmounts: List<Double>
    )

    private fun extractNotificationAmounts(
        title: String?,
        fullText: String,
        pattern: Pattern
    ): ExtractedNotificationAmounts? {
        val results = mutableListOf<MatchResultData>()

        if (!title.isNullOrBlank()) {
            val titleMatcher = pattern.matcher(title)
            while (titleMatcher.find()) {
                val rawStr = titleMatcher.group(1) ?: continue
                val amountStr = rawStr.replace(",", ".")
                val currency = titleMatcher.group(2)
                val amount = amountStr.toDoubleOrNull() ?: continue
                results.add(
                    MatchResultData(
                        amount = amount,
                        currency = currency,
                        isTitleMatch = true,
                        isSigned = rawStr.startsWith("-") || rawStr.startsWith("+")
                    )
                )
            }
        }

        val fullMatcher = pattern.matcher(fullText)
        while (fullMatcher.find()) {
            val rawStr = fullMatcher.group(1) ?: continue
            val amountStr = rawStr.replace(",", ".")
            val currency = fullMatcher.group(2)
            val amount = amountStr.toDoubleOrNull() ?: continue
            results.add(
                MatchResultData(
                    amount = amount,
                    currency = currency,
                    isTitleMatch = false,
                    isSigned = rawStr.startsWith("-") || rawStr.startsWith("+")
                )
            )
        }

        if (results.isEmpty()) return null

        val best = results.firstOrNull { it.isTitleMatch && it.isSigned }
            ?: results.firstOrNull { it.isSigned }
            ?: results.firstOrNull { it.isTitleMatch }
            ?: results.first()

        val alts = results
            .map { it.amount }
            .filter { it != best.amount }
            .distinct()

        return ExtractedNotificationAmounts(
            mainMatch = best,
            alternativeAmounts = alts
        )
    }
}
