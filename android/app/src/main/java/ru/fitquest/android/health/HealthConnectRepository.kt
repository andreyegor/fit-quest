package ru.fitquest.android.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter

import java.time.Instant

class HealthConnectRepository(context: Context) {
    private val client = HealthConnectClient.getOrCreate(context)

    suspend fun getNewTrainingsDto(since: Instant): List<TrainingDto> {
        return getNewTrainings(since).mapNotNull { exercise ->
            TrainingDto.fromExerciseSessionRecord(exercise, client)
        }
    }

    private suspend fun getNewTrainings(since: Instant): List<ExerciseSessionRecord> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.after(since),
                ascendingOrder = true
            )
        )
        return response.records
    }
}
