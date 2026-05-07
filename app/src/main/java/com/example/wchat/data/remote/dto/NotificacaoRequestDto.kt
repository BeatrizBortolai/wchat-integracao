package com.example.wchat.data.remote.dto

data class NotificacaoRequestDto(
    val titulo: String,
    val descricao: String,
    val nomeCampanha: String,
    val remetenteId: String,
    val linkEvento: String? = null,
    val urlSaberMais: String? = null,
    val urlInscrever: String? = null,
    val deeplink: String? = null,
    val grupo: String? = null,
    val segmento: String? = null,
    val destinatariosIds: List<String> = emptyList(),
    val tipoDestinatario: String
)
