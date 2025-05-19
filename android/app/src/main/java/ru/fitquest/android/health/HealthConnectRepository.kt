package ru.fitquest.android.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

class HealthConnectRepository(private val context: Context) {

    private val healthConnectClient = HealthConnectClient.getOrCreate(context)

    suspend fun getNewTrainings(since: Instant): List<ExerciseSessionRecord> {
        val request = ReadRecordsRequest<ExerciseSessionRecord>(
            timeRangeFilter = TimeRangeFilter.after(since),
            ascendingOrder = true,
            pageSize = 10
        )
        val response = healthConnectClient.readRecords(request)

        return response.records
    }
}
