package com.example.wchat.model

import java.util.Date

data class Mensagem(
    val id: String = "",
    val texto: String = "",
    val remetenteId: String = "",
    val remetenteNome: String = "",
    val destinatarioId: String? = null,
    val idEnvio: String = "",
    val remetenteTipo: String = "",
    val timestamp: Date? = null,
    val lida: Boolean = false,
    val lidoPor: List<String> = emptyList()
)
