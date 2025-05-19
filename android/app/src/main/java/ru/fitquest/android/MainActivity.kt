package ru.fitquest.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.fitquest.android.auth.TokensManager
import ru.fitquest.android.health.TrainingSyncWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 12345
    }

    private lateinit var lastSyncText: TextView
    private lateinit var logoutButton: Button
    private lateinit var syncButton: Button

    // Набор Health Connect разрешений
    private val healthPermissions = setOf(
        HealthPermission.getReadPermission(androidx.health.connect.client.records.ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(androidx.health.connect.client.records.StepsRecord::class),
        HealthPermission.getReadPermission(androidx.health.connect.client.records.DistanceRecord::class),
        HealthPermission.getReadPermission(androidx.health.connect.client.records.ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(androidx.health.connect.client.records.HeartRateRecord::class)
    )

    private val permissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions: Set<String> ->
        if (grantedPermissions.containsAll(healthPermissions)) {
            runImmediateSync()
        } else {
            // :(
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)


        val tokensManager = TokensManager(this)
        if (!tokensManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            setContentView(R.layout.activity_main)
        }

        setContentView(R.layout.activity_main)

        lastSyncText = findViewById(R.id.last_sync_text)
        logoutButton = findViewById(R.id.logout_button)
        syncButton   = findViewById(R.id.sync_button)

        logoutButton.setOnClickListener {
            tokensManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        syncButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
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
        val needed  = healthPermissions - granted

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
        val prefs = getSharedPreferences("training_sync", Context.MODE_PRIVATE)
        val ts    = prefs.getLong("last_sync_time", -1L)
        val text  = if (ts != -1L) {
            val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            "Дата последней синхронизации: ${fmt.format(Date(ts))}"
        } else {
            "Дата последней синхронизации: Не синхронизировано"
        }
        lastSyncText.text = text
    }
}