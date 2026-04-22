package com.example.wchat.data.remote.dto

data class AuthSyncRequestDto(
    val id: String,
    val nome: String,
    val email: String,
    val password: String,
    val tipo: String,
    val cargo: String?,
    val segmentos: List<String>
)