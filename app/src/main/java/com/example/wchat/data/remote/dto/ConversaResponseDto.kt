package com.example.wchat.data.remote.dto

data class ConversaResponseDto(
    val usuarioId: String? = null,
    val nome: String? = null,
    val grupoId: String? = null,
    val nomeGrupo: String? = null,
    val ultimaMensagem: String? = null,
    val dataUltimaMensagem: String? = null,
    val naoLidas: Long = 0
)
