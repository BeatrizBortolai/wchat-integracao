package com.example.wchat.data.remote.dto

data class SegmentoDto(
    val id: String = "",
    val nome: String = "",
    val descricao: String? = null,
    val participantesIds: List<String> = emptyList()
)