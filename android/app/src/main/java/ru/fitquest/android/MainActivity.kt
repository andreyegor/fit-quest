package ru.fitquest.android

import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import ru.fitquest.android.auth.TokensManager
import ru.fitquest.android.health.TrainingSyncWorker
import java.util.concurrent.TimeUnit
import androidx.core.net.toUri
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope

import ru.fitquest.android.health.SyncPreferences
import java.time.Instant

class MainActivity : AppCompatActivity() {
    private lateinit var lastSyncText: TextView
    private lateinit var logoutButton: Button
    private lateinit var syncButton: Button

    private val healthPermissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),

        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),

        HealthPermission.getReadPermission(SpeedRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    private val permissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions: Set<String> ->
        if (grantedPermissions.containsAll(healthPermissions)) {
            runImmediateSync()
        } else {
            Log.d("Permissions", "Not all permissions granted :(")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        installSplashScreen()
        super.onCreate(savedInstanceState)

        val tokensManager = TokensManager(this)
        if (!tokensManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        lastSyncText = findViewById(R.id.last_sync_text)
        logoutButton = findViewById(R.id.logout_button)
        syncButton = findViewById(R.id.sync_button)

        logoutButton.setOnClickListener {
            tokensManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        syncButton.setOnClickListener {
            lifecycleScope.launch {
                ensureHealthPermissions()
            }

        }

        schedulePeriodicSync()
        updateLastSyncText()
    }

    private fun schedulePeriodicSync() {
        val periodic = PeriodicWorkRequestBuilder<TrainingSyncWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "training_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )
    }

    private suspend fun ensureHealthPermissions() {
        val client = HealthConnectClient.getOrCreate(this)
        val granted = client.permissionController.getGrantedPermissions()
        val needed = healthPermissions - granted


        val isHealthConnectAvailable = try {
            packageManager.getPackageInfo("com.google.android.apps.healthdata", 0)
            true
        } catch (e: Exception) {
            false
        }

        if (!isHealthConnectAvailable) {
            Log.d("Permissions", "Health Connect not installed")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data =
                    "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata".toUri()
                setPackage("com.android.vending")
            }
            startActivity(intent)
            return
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed)
        } else {
            runImmediateSync()
        }
    }

    private fun runImmediateSync() {
        val oneTime = OneTimeWorkRequestBuilder<TrainingSyncWorker>().build()
        WorkManager.getInstance(this).enqueue(oneTime)
        lastSyncText.postDelayed({ updateLastSyncText() }, 3_000)
    }

    private fun updateLastSyncText() {
        val prefs = SyncPreferences(this)
        val ts = prefs.lastSyncTime
        val text = if (ts != Instant.ofEpochMilli(0L)) {
            "Дата последней синхронизации: $ts"
        } else {
            "Дата последней синхронизации: Не синхронизировано"
        }
        lastSyncText.text = text
    }
}