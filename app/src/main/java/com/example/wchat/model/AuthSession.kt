package com.example.wchat.model

data class AuthSession(
    val token: String,
    val usuarioId: String,
    val nome: String,
    val email: String
)