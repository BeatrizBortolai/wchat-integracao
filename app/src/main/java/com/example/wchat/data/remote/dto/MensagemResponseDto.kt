package com.example.wchat.data.remote.dto

data class MensagemResponseDto(
    val id: String = "",
    val remetenteId: String = "",
    val remetenteNome: String = "",
    val destinatarioId: String? = null,
    val grupoId: String? = null,
    val texto: String = "",
    val tipoMensagem: String = "TEXT",
    val urlAnexo: String? = null,
    val lida: Boolean = false,
    val status: String? = null,
    val dataEnvio: String? = null,
    val dataEntrega: String? = null,
    val dataLeitura: String? = null
)