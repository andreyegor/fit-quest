package ru.fitquest.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class HealthConnectRationaleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HealthRationale", "HealthConnectRationaleActivity была запущена.")
        finish()
    }
}