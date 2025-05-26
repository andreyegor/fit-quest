package ru.fitquest.android.health

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Length

import java.time.Instant

data class TrainingDto(
    val exerciseType: String,
    val startTime: String,
    val endTime: String,
    val metrics: Map<String, Double>,
    val series: Map<String, List<Double>>,
    val route: List<LocationPoint>?
) {
    companion object {
        suspend fun fromExerciseSessionRecord(
            record: ExerciseSessionRecord,
            client: HealthConnectClient
        ): TrainingDto? {
            val config = typeToConfig[record.exerciseType] ?: return null

            val metricsMap = mutableMapOf<String, Double>()
            for (metric in config.metrics) {
                val value = MetricsProviders.get(metric, client, record)
                metricsMap[metric] = value
            }

            //TODO ЗАПЛАТКА
            //В этой реализации игнорируется timestamp{} который, наверное, важен
            //Сделано так как бэк уже готов и фрон работает со старыми данными
            // НО переделать
            val seriesMap = mutableMapOf<String, List<Double>>()
            for (series in config.series) {
                val points = SeriesProviders.get(series, client, record)
                seriesMap[series] = points.map { it.value }
            }

            val routePoints: List<LocationPoint>? =
                if (config.includeRoute) null else null

            return TrainingDto(
                exerciseType = config.type,
                startTime = record.startTime.toString(),
                endTime = record.endTime.toString(),
                metrics = metricsMap,
                series = seriesMap,
                route = routePoints
            )
        }

        private data class ExerciseConfig(
            val type: String,
            val metrics: List<String>,
            val series: List<String>,
            val includeRoute: Boolean
        )

        private val typeToConfig: Map<Int, ExerciseConfig> = mapOf(
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING to ExerciseConfig(
                type = "running",
                metrics = listOf("distanceMeters", "caloriesKcal", "steps"),
                series = listOf("speedSeries", "heartRateSeries"),
                includeRoute = true
            ),
            ExerciseSessionRecord.EXERCISE_TYPE_WALKING to ExerciseConfig(
                type = "walking",
                metrics = listOf("distanceMeters", "steps"),
                series = listOf("speedSeries", "heartRateSeries"),
                includeRoute = true
            ),
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING to ExerciseConfig(
                type = "cycling",
                metrics = listOf("distanceMeters", "caloriesKcal"),
                series = listOf("speedSeries", "heartRateSeries"),
                includeRoute = true
            ),
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL to ExerciseConfig(
                type = "swimming",
                metrics = listOf("distanceMeters", "caloriesKcal"),
                series = listOf("heartRateSeries"),
                includeRoute = false
            )
        )
    }
}

object MetricsProviders {
    suspend fun get(
        name: String,
        client: HealthConnectClient,
        record: ExerciseSessionRecord
    ): Double = when (name) {
        "distanceMeters" -> aggregateDouble(
            client,
            DistanceRecord.DISTANCE_TOTAL,
            record.startTime,
            record.endTime
        )

        "caloriesKcal" -> aggregateCalories(
            client,
            ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
            record.startTime,
            record.endTime
        )

        "steps" -> aggregateLong(
            client,
            StepsRecord.COUNT_TOTAL,
            record.startTime,
            record.endTime
        )

        else -> 0.0
    }

    private suspend fun aggregateDouble(
        client: HealthConnectClient,
        metric: AggregateMetric<Length>,
        start: Instant,
        end: Instant
    ): Double {
        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(metric),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )

        return response.doubleValues[metric.toString()] ?: 0.0
    }

    private suspend fun aggregateLong(
        client: HealthConnectClient,
        metric: AggregateMetric<Long>,
        start: Instant,
        end: Instant
    ): Double {
        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(metric),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )

        return response.longValues[metric.toString()]?.toDouble() ?: 0.0
    }

    private suspend fun aggregateCalories(
        client: HealthConnectClient,
        metric: AggregateMetric<androidx.health.connect.client.units.Energy>,
        start: Instant,
        end: Instant
    ): Double {
        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(metric),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.doubleValues[metric.toString()] ?: 0.0
    }
}

object SeriesProviders {
    suspend fun get(
        name: String,
        client: HealthConnectClient,
        session: ExerciseSessionRecord
    ): List<DataPoint> = when (name) {
        "speedSeries" -> readSpeedSeries(client, session.startTime, session.endTime)
        "heartRateSeries" -> readHeartRateSeries(client, session.startTime, session.endTime)
        else -> emptyList()
    }

    private suspend fun readSpeedSeries(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): List<DataPoint> {
        val request = ReadRecordsRequest(
            recordType = SpeedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return client.readRecords(request)
            .records
            .flatMap { rec ->
                rec.samples.map { sample ->
                    DataPoint(sample.time, sample.speed.inMetersPerSecond)
                }
            }
            .sortedBy { it.timestamp }
    }

    private suspend fun readHeartRateSeries(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): List<DataPoint> {
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return client.readRecords(request)
            .records
            .flatMap { rec ->
                rec.samples.map { sample ->
                    DataPoint(sample.time, sample.beatsPerMinute.toDouble())
                }
            }
            .sortedBy { it.timestamp }
    }
}

data class DataPoint(val timestamp: Instant, val value: Double)

data class LocationPoint(val latitude: Double, val longitude: Double, val timestamp: Instant)
