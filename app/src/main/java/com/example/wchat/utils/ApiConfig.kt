package com.example.wchat.utils

import android.os.Build
import android.util.Log

object ApiConfig {

    fun getBaseUrl(): String {

        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()

        Log.d(
            "DEVICE_INFO",
            "manufacturer=$manufacturer | model=$model"
        )

        return if (
            manufacturer.contains("genymobile")
        ) {
            "http://192.168.56.1:8080/"
        } else {
            "http://10.0.2.2:8080/"
        }
    }
}