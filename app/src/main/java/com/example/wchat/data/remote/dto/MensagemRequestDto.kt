package com.example.wchat.data.remote.dto

data class MensagemRequestDto(
    val remetenteId: String,
    val destinatarioId: String? = null,
    val grupoId: String? = null,
    val segmentoId: String? = null,
    val texto: String,
    val tipoMensagem: String = "TEXT",
    val urlAnexo: String? = null
)