package ru.fitquest.android.health

import android.content.Context
import java.time.Instant
import androidx.core.content.edit

class SyncPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    var lastSyncTime: Instant
        get() = Instant.ofEpochMilli(prefs.getLong("last_sync", 0L))
        set(value) {
            prefs.edit { putLong("last_sync", value.toEpochMilli()) }
        }
}
