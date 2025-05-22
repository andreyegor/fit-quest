package ru.fitquest.android.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

import ru.fitquest.android.health.TrainingDto

data class LoginRequest(val email: String, val password: String)
data class TokenResponse(val accessToken: String, val refreshToken: String)
data class RefreshRequest(val accessToken: String, val refreshToken: String)

interface Api {
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<TokenResponse>

    @POST("auth/refresh")
    fun refresh(@Body request: RefreshRequest): Call<TokenResponse>

    @POST("auth/logout")
    fun logout(@Header("Authorization") accessToken: String): Call<Void>

    @POST("trainings/upload")
    fun uploadTrainings(
        @Header("Authorization") accessToken: String,
        @Body trainings: List<TrainingDto>
    ): Call<Void>
}