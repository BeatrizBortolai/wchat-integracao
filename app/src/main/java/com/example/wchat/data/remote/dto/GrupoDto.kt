package com.example.wchat.data.remote.dto

data class GrupoDto(
    val id: String = "",
    val nome: String = "",
    val participantesIds: List<String> = emptyList(),
    val dataCriacao: String? = null
)