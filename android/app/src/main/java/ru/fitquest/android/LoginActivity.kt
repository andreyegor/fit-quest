package ru.fitquest.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import ru.fitquest.android.api.ApiService
import ru.fitquest.android.api.LoginRequest
import ru.fitquest.android.api.TokenResponse
import ru.fitquest.android.auth.TokensManager

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var tokensManager: TokensManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_button)
        tokensManager = TokensManager(this)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                showError("Введите почту и пароль")
            }
        }
    }

    private fun login(email: String, password: String) {
        val request = LoginRequest(email, password)

        ApiService.api.login(request).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { tokenResponse ->
                        tokensManager.save(tokenResponse.accessToken, tokenResponse.refreshToken)

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    showError("Неверный логин или пароль")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                showError("Ошибка подключения")
            }
        })
    }

    private fun showError(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }
}
