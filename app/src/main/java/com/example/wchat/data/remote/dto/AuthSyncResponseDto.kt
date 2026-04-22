package com.example.wchat.data.remote.dto

data class AuthSyncResponseDto(
    val token: String,
    val usuarioId: String,
    val nome: String,
    val email: String
)