package com.example.wchat.data.remote.dto

data class UsuarioUpdateRequestDto(
    val nome: String? = null,
    val cargo: String? = null,
    val anotacoesOperador: String? = null
)