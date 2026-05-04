package com.example.wchat.data.remote.dto

data class NotificacaoResponseDto(
    val id: String = "",
    val titulo: String = "",
    val nomeCampanha: String = "",
    val totalEnviados: Int = 0,
    val dataEnvio: String? = null
)