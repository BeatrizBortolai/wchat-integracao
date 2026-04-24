package com.example.wchat.data.remote.dto

data class UsuarioResponseDto(
    val id: String,
    val nome: String,
    val email: String,
    val tipo: String,
    val cargo: String?,
    val segmentos: List<String>?,
    val fcmToken: String?
)