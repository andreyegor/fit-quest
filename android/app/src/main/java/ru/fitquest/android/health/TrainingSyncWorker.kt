package ru.fitquest.android.health

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import java.time.Instant

import ru.fitquest.android.auth.AuthService
import ru.fitquest.android.auth.TokensManager
import ru.fitquest.android.api.ApiService

class TrainingSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val prefs = SyncPreferences(context)
    private val repo = HealthConnectRepository(context)
    private val tokensManager = TokensManager(context)

    override suspend fun doWork(): Result {
        Log.d("TrainingSyncWorker", "doWork do work")
        try {
            val lastSync = prefs.lastSyncTime
            val newTrainings = repo.getNewTrainingsDto(lastSync)

            if (newTrainings.isEmpty()) {
                prefs.lastSyncTime = Instant.now()
                return Result.success()}

            if (tokensManager.getAccess() == null) {
                AuthService.refresh(tokensManager)
            }

            val request = { accessToken: String ->
                ApiService.api.uploadTrainings(
                    "Bearer $accessToken",
                    newTrainings
                )
            }
            Log.d("TrainingJSON", Gson().toJson(newTrainings))

            var response = request(tokensManager.getAccess() ?: return Result.retry()).execute()

            if (response.code() == 401) {
                AuthService.refresh(tokensManager)
                response = request(tokensManager.getAccess() ?: return Result.retry()).execute()
            }

            if (response.isSuccessful) {
                prefs.lastSyncTime = Instant.now()
                return Result.success()
            } else {
                return Result.retry()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

}

