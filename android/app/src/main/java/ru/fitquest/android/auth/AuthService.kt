package ru.fitquest.android.auth

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import ru.fitquest.android.api.*

object AuthService{
    fun login(username: String, password: String, tokensManager: TokensManager) {
        val request = LoginRequest(username, password)

        ApiService.api.login(request).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    tokenResponse?.let {
                        tokensManager.save(it.accessToken, it.refreshToken)
                    }
                } else {
                    // TODO wrong info
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                // TODO network error
            }
        })
    }

    fun refresh(tokensManager: TokensManager) {
        val accessToken = tokensManager.getAccess()
        val refreshToken = tokensManager.getRefresh()

        if (accessToken != null && refreshToken != null) {
            val request = RefreshRequest(accessToken, refreshToken)

            ApiService.api.refresh(request).enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful) {
                        val tokenResponse = response.body()
                        tokenResponse?.let {
                            tokensManager.save(it.accessToken, it.refreshToken)
                        }
                    } else {
                        // TODO token is invalid
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    // TODO network error
                }
            })
        } else {
            // TODO refresh not found
        }
    }

    fun logout(tokensManager: TokensManager) {
        val accessToken = tokensManager.getAccess()

        if (accessToken != null) {
            ApiService.api.logout("Bearer $accessToken").enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        tokensManager.clear()
                        // TODO to login page
                    } else {
                        // TODO сервер вернул чё-то странное, ну и не важно
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // TODO network error
                }
            })
        } else {
            tokensManager.clear()
        }

        // Закрываем текущую активность, чтобы вернуться к экрану логина
    }


}
