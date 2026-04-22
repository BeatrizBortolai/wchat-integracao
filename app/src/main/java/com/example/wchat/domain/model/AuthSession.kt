package com.example.wchat.domain.model

data class AuthSession(
    val token: String,
    val usuarioId: String,
    val nome: String,
    val email: String
)