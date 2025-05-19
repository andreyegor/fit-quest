package ru.fitquest.android.health

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.Instant

import ru.fitquest.android.api.toDto
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
        try {
            val lastSync = prefs.lastSyncTime
            val newTrainings = repo.getNewTrainings(lastSync)

            if (newTrainings.isEmpty()) return Result.success()

            if (tokensManager.getAccess() == null) {
                AuthService.refresh(tokensManager)
            }

            val dtoList = newTrainings.map { it.toDto() }

            val request = {accessToken:String -> ApiService.api.uploadTrainings("Bearer $accessToken", dtoList)}

            var response = request(tokensManager.getAccess() ?: return Result.retry()).execute()

            if (response.code() == 401){
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

