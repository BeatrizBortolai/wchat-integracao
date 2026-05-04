package com.example.wchat.data.remote.dto

data class EnvioSegmentoRequestDto(
    val remetenteId: String,
    val segmento: String,
    val texto: String,
    val tipoMensagem: String = "TEXT",
    val urlAnexo: String? = null
)