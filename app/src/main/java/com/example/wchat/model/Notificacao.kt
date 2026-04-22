package com.example.wchat.model

data class Notificacao(
    val titulo: String = "",
    val descricao: String = "",
    val nomeCampanha: String = "",
    val linkEvento: String = "",
    val urlSaberMais: String = "",
    val urlInscrever: String = ""
)