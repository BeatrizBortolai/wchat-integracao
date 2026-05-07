package com.example.wchat.data.remote.dto

data class ConversaResponseDto(
    val id: String? = null,
    val nome: String? = null,
    val tipo: String? = null,
    val ultimaMensagem: MensagemResponseDto? = null,
    val usuarioId: String? = null,
    val grupoId: String? = null,
    val nomeGrupo: String? = null,
    val segmentoId: String? = null,
    val nomeSegmento: String? = null,
    val dataUltimaMensagem: String? = null,
    val naoLidas: Long = 0
)
